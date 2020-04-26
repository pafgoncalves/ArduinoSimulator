/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.isec.deis.mis.arduinosimulator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import pt.isec.deis.mis.arduinosimulator.instructions.BREAK;
import pt.isec.deis.mis.arduinosimulator.peripherals.*;
import pt.isec.deis.mis.arduinosimulator.utils.Utils;

/**
 *
 * @author pafgoncalves@ipc.pt
 */
public abstract class CPU {
    
    protected static final int BREAK_OPCODE = 0x9598;
    
    protected final DataMemory sram;
    protected final ProgramMemory flash;
    protected final InstructionDecoder instructionDecoder;
    protected final int pcMaxSize;
    
    protected final InstructionSetType isType;
    
    protected List<Peripheral> peripherals;
    
    protected int pc;
    protected int previousPc;
    
    protected boolean interrupt = false;

    protected boolean incInDisassemble = true;
    protected boolean running = false;
    protected boolean log = false;
    protected boolean ignoreInstructionCycles = true;
    
    protected long cyclesCount = 0;
    protected int instructionCycles = 0;
//    protected long[] executionTimes = new long[184_000_000];
    protected long[] executionTimes = new long[0];

    protected long startTime = 0;
    
    protected Map<Integer,BREAK> breakpoints = new HashMap<>();
    
    protected List<PinChangedListener> pinChangedListeners = new ArrayList<>();
    protected List<CPUStatusListener> cpuStatusListeners = new ArrayList<>();
    
    protected Random random = new Random();
    
    private Thread nonPullUpThread = null;
    
    
    public CPU(InstructionSetType isType, DataMemory sram, ProgramMemory flash, int pcMaxSize) {
        this.instructionDecoder = new InstructionDecoder();
        pc = 0;
        
        this.isType = isType;
        this.sram = sram;
        this.flash = flash;
        this.pcMaxSize = pcMaxSize;
        
        peripherals = new ArrayList<>();
    }

    public abstract String getRegisterName(int addr);
    
    public InstructionSetType getInstructionSetType() {
        return isType;
    }
    
    public int getPCSize() {
        if( pcMaxSize>0xFFFF ) {
            return 22;
        }
        return 16;
    }
    
    public abstract Float getInputPinValue(int port, int pin);
    
    //TODO: a transformação de PORTA para PINO deveria ocorrer numa classe
    //      que encapsule o CPU num ARDUINO
    //      Fica para trabalho futuro
    public abstract void setInputPinValue(int pin, Float value);
    
    
    public Usart getUsart() {
        for(Peripheral p : peripherals) {
            if(p instanceof Usart) {
                return (Usart)p;
            }
        }
        return null;
    }
    
    public DataMemory getSRAM() {
        return sram;
    }

    public ProgramMemory getFLASH() {
        return flash;
    }
    
    public int getPc() {
        return pc;
    }
    
    public int getPreviousPc() {
        return previousPc;
    }
    
    public void setPc(int pc) {
        previousPc = this.pc;
        this.pc = pc & pcMaxSize;
    }
    
    public void incPc() {
        setPc(getPc()+1);
    }
    
    public int fetch() {
        return getFLASH().get(getPc());
    }
    
    public int fetchNext() {
        return getFLASH().get(getPc()+1);
    }

    public InstructionDecoder getInstructionDecoder() {
        return instructionDecoder;
    }
    
    public void setIncInDisassemble(boolean incInDisassemble) {
        this.incInDisassemble = incInDisassemble;
    }
    
    public boolean getIncInDisassemble() {
        return incInDisassemble;
    }
    
    public void setLog(boolean log) {
        this.log = log;
    }

    public boolean getLog() {
        return log;
    }
    
    public void addBreakpoint(int address) {
        int instructionValue = getFLASH().get(address);
        if( instructionValue==BREAK_OPCODE ) {
            return;
        }
        BREAK breakInst = new BREAK(instructionValue);
        breakpoints.put(address, breakInst);
        getFLASH().set(address, BREAK_OPCODE);
    }

    public void removeBreakpoint(int address) {
        BREAK inst = breakpoints.remove(address);
        if( inst!=null ) {
            getFLASH().set(address, inst.getPreviousIntructionValue());
        }
    }

    public void removeAllBreakpoints() {
        for(int address : breakpoints.keySet()) {
            BREAK inst = breakpoints.remove(address);
            if( inst!=null ) {
                getFLASH().set(address, inst.getPreviousIntructionValue());
            }
        }
        breakpoints.clear();
    }
    
    public List<Integer> getBreakpoints() {
        return new ArrayList<>(breakpoints.keySet());
    }
    
    public void setInstructionCycles(int numCycles) {
        this.instructionCycles = numCycles-1;
    }
    
    public void resetCyclesCount() {
        cyclesCount = 0;
    }
    
    public long getCyclesExecuted() {
        return cyclesCount;
    }
    
    private long lastInterrupt = 0;
    private long timeNanos = 0;
    private long interruptCount = 0;
    long lastCallTime;
    public synchronized void execute() throws Exception {
        cyclesCount++;


        clock();
        
        //isto deve ser testado antes de executar a instrução para quando 
        //      existe mais do que uma interrupção em simultaneo, entrar 
        //      directamente para a interrupção seguinte sem executar nenhuma
        //      instrução entre as duas interrupções
        if( interrupt && getSRAM().getStatusRegisterObj().getGlobalInterruptEnable() ) {
            
//            if( true ) throw new IllegalAccessException();
            int previousPC = getPc();
            if( checkInterrupt() ) {
                if( getPCSize()==16 ) {
                    getSRAM().setStackW(getSRAM().getStackPointer(), previousPC);
                    getSRAM().setStackPointer(getSRAM().getStackPointer()-2);
                } else {
                    getSRAM().setStack3(getSRAM().getStackPointer(), previousPC);
                    getSRAM().setStackPointer(getSRAM().getStackPointer()-3);
                }
                //durante o tratamento de uma interrupção as interrupções estão desactivadas
                //a instrução RETI volta a fazer o enable
                getSRAM().getStatusRegisterObj().setGlobalInterruptEnable(false);
            } else {
                //só se deve desactivar quando estiverem todas tratadas
                interrupt = false;
            }
            
        }

        
        if( log ) {
            System.out.printf("%04X: ",pc);
        }
        
        if( !ignoreInstructionCycles && instructionCycles>0 ) {
            instructionCycles--;
            if( log ) {
                System.out.println("");
            }
        } else {
            int instructionValue = getFLASH().get(getPc());
            Instruction instruction;
            
            //TODO: para testar: executar sempre o BREAK, que lança excepção,
            //      apanha-se aqui e vai-se ao mapa de breakpoints verificar se 
            //      o endereço tem lá outra instrução e executa-se a mesma. Tem
            //      de se implementar a lógica do 'inBreak' aqui.
            //      No addBreakpoint substitui-se o opcode na flash e tem de se 
            //      repor no removeBreakpoint
            if( instructionValue==BREAK_OPCODE ) {
                instruction = breakpoints.get(getPc());
                if( instruction==null ) {
                    instruction = new BREAK();
                    breakpoints.put(getPc(), (BREAK)instruction);
                }
            } else {
                instruction = instructionDecoder.getInstruction(instructionValue);
            }
//            InstructionParams params = instruction.decode(this, instructionValue);
            InstructionParams params = getFLASH().getInstructionParams(getPc());
            if( log ) {
                System.out.println(instruction.getASM(this, params.op1, params.op2));
            }
            instruction.execute(this, params.op1, params.op2);
        }
        
        
//        long now = System.nanoTime();
//        //if( now-lastCallTime > 200_000 ) System.out.println(": "+(now-lastCallTime));
//        executionTimes[(int)(cyclesCount-1)] = (now-lastCallTime);
//        lastCallTime = now;

    }
    
    protected abstract boolean checkInterrupt();
    
    protected void clock() {
        for(Peripheral p : peripherals) {
            p.clock();
        }
    }
    
    public void interrupt() {
        interrupt = true;
    }
    
    protected abstract Runnable getPullUpRunnable();
    
    public void run() throws Exception {
        if( running ) {
            return;
        }
        for(Peripheral p : peripherals) {
            p.start();
        }
        running = true;
        
        nonPullUpThread = new Thread(getPullUpRunnable(),"nonPullUpThread");
        nonPullUpThread.setDaemon(true);
        nonPullUpThread.start();        
        
        fireCPUStatusListener(running);
        startTime = System.currentTimeMillis();
        cyclesCount = 0;
        lastCallTime = System.nanoTime();
        while( running ) {
            try {
                execute();
            } catch(Exception e) {
                stop();
//                long now = System.currentTimeMillis();
//                System.out.println("time: "+(now-startTime));
//                System.out.println("ciclos: "+cyclesCount);
//                System.out.println("velocidade: "+(cyclesCount/((now-startTime)*1000.0))+" MHz");
                throw e;
            }
        }
        
    }
    
    public double getSpeed() {
        if( running ) {
            return (cyclesCount/((System.currentTimeMillis()-startTime)*1000.0));
        }
        return 0.0;
    }
    
    public boolean isRunning() {
        return running;
    }
    
    public void stop() {
        for(Peripheral p : peripherals) {
            p.stop();
        }
        running = false;
        fireCPUStatusListener(running);
    }

    public void reset() {
        Arrays.fill(executionTimes, -1);
        cyclesCount=0;
        setPc(0);
        previousPc = 0;
        getSRAM().reset();
        for(Peripheral p : peripherals) {
            p.reset();
        }
    }
    
    public long[] getExecutionTimes() {
        return executionTimes;
    }
    
    
    public void shutdown() {
        stop();
        getSRAM().shutdown();
    }
    
    
    public synchronized void disassemble(InstructionDecoder decoder, int start, int end) throws Exception {
        setPc(start);
        while(true) {
            int pc = getPc();
            int opcode = fetch();
            System.out.printf("%04X: %04X\t",pc, opcode);
            try {
                Instruction instruction = decoder.getInstruction(opcode);
                InstructionParams params = instruction.decode(this, opcode);
                System.out.printf("%s", instruction.getASM(this,params.op1, params.op2));
                if( pc <= 0x32 ) {
                    switch( pc ) {
                        case 0x00:
                            System.out.print("\t//RESET");
                            break;
                        case 0x02:
                            System.out.print("\t//INT0");
                            break;
                        case 0x04:
                            System.out.print("\t//INT1");
                            break;
                        case 0x06:
                            System.out.print("\t//PCINT0");
                            break;
                        case 0x08:
                            System.out.print("\t//PCINT1");
                            break;
                        case 0x0A:
                            System.out.print("\t//PCINT2");
                            break;
                        case 0x0C:
                            System.out.print("\t//WDT");
                            break;
                        case 0x0E:
                            System.out.print("\t//TIMER2_COMPA");
                            break;
                        case 0x10:
                            System.out.print("\t//TIMER2_COMPB");
                            break;
                        case 0x12:
                            System.out.print("\t//TIMER2_OVF");
                            break;
                        case 0x14:
                            System.out.print("\t//TIMER1_CAPT");
                            break;
                        case 0x16:
                            System.out.print("\t//TIMER1_COMPA");
                            break;
                        case 0x18:
                            System.out.print("\t//TIMER1_COMPB");
                            break;
                        case 0x1A:
                            System.out.print("\t//TIMER1_OVF");
                            break;
                        case 0x1C:
                            System.out.print("\t//TIMER0_COMPA");
                            break;
                        case 0x1E:
                            System.out.print("\t//TIMER0_COMPB");
                            break;
                        case 0x20:
                            System.out.print("\t//TIMER0_OVF");
                            break;
                        case 0x22:
                            System.out.print("\t//SPI STC");
                            break;
                        case 0x24:
                            System.out.print("\t//USART_RX");
                            break;
                        case 0x26:
                            System.out.print("\t//USART_UDRE");
                            break;
                        case 0x28:
                            System.out.print("\t//USART_TX");
                            break;
                        case 0x2A:
                            System.out.print("\t//ADC");
                            break;
                        case 0x2C:
                            System.out.print("\t//EE READY");
                            break;
                        case 0x2E:
                            System.out.print("\t//ANALOG COMP");
                            break;
                        case 0x30:
                            System.out.print("\t//TWI");
                            break;
                        case 0x32:
                            System.out.print("\t//SPM READY");
                            break;
                    }
                }
            } catch(IllegalInstructionException ex) {
                setPc(pc+1);
                System.out.print(".word "+Utils.toHex(opcode,4));
            }
            System.out.println("");
            if( getPc()>=end ) {
                break;
            }
        }
        setPc(start);
    }

    public synchronized List<String> disassemble() throws Exception {
        List<String> list = new ArrayList<>();
        int currentPc = getPc();
        boolean inc = getIncInDisassemble();
        setIncInDisassemble(true);
        setPc(0);
        int end = getFLASH().loadedSize();

        while(true) {
            int localpc = getPc();
            int opcode = fetch();
//            String line = String.format("%04X: %04X\t",localpc, opcode);
            String line = String.format("%04X: ",localpc);
            try {
                Instruction instruction = getInstructionDecoder().getInstruction(opcode);
                if( opcode==BREAK_OPCODE ) {
                    if( breakpoints.containsKey(localpc) ) {
                        instruction = breakpoints.get(localpc);
                    }
                }
                InstructionParams params = instruction.decode(this, opcode);
                line += String.format("%s", instruction.getASM(this,params.op1, params.op2));
                if( localpc <= 0x32 ) {
                    switch( localpc ) {
                        case 0x00:
                            line += String.format("\t//RESET");
                            break;
                        case 0x02:
                            line += String.format("\t//INT0");
                            break;
                        case 0x04:
                            line += String.format("\t//INT1");
                            break;
                        case 0x06:
                            line += String.format("\t//PCINT0");
                            break;
                        case 0x08:
                            line += String.format("\t//PCINT1");
                            break;
                        case 0x0A:
                            line += String.format("\t//PCINT2");
                            break;
                        case 0x0C:
                            line += String.format("\t//WDT");
                            break;
                        case 0x0E:
                            line += String.format("\t//TIMER2_COMPA");
                            break;
                        case 0x10:
                            line += String.format("\t//TIMER2_COMPB");
                            break;
                        case 0x12:
                            line += String.format("\t//TIMER2_OVF");
                            break;
                        case 0x14:
                            line += String.format("\t//TIMER1_CAPT");
                            break;
                        case 0x16:
                            line += String.format("\t//TIMER1_COMPA");
                            break;
                        case 0x18:
                            line += String.format("\t//TIMER1_COMPB");
                            break;
                        case 0x1A:
                            line += String.format("\t//TIMER1_OVF");
                            break;
                        case 0x1C:
                            line += String.format("\t//TIMER0_COMPA");
                            break;
                        case 0x1E:
                            line += String.format("\t//TIMER0_COMPB");
                            break;
                        case 0x20:
                            line += String.format("\t//TIMER0_OVF");
                            break;
                        case 0x22:
                            line += String.format("\t//SPI STC");
                            break;
                        case 0x24:
                            line += String.format("\t//USART_RX");
                            break;
                        case 0x26:
                            line += String.format("\t//USART_UDRE");
                            break;
                        case 0x28:
                            line += String.format("\t//USART_TX");
                            break;
                        case 0x2A:
                            line += String.format("\t//ADC");
                            break;
                        case 0x2C:
                            line += String.format("\t//EE READY");
                            break;
                        case 0x2E:
                            line += String.format("\t//ANALOG COMP");
                            break;
                        case 0x30:
                            line += String.format("\t//TWI");
                            break;
                        case 0x32:
                            line += String.format("\t//SPM READY");
                            break;
                    }
                }
            } catch(IllegalInstructionException ex) {
                setPc(localpc+1);
                line += String.format(".word "+Utils.toHex(opcode,4));
            }
            list.add(line);
            if( getPc()>=end ) {
                break;
            }
        }
        setIncInDisassemble(inc);
        setPc(currentPc);
        return list;
    }
    
    
    
    public void addPinChangedListener(PinChangedListener l) {
        if( !pinChangedListeners.contains(l) ) {
            pinChangedListeners.add(l);
        }
    }
    
    public void removePinChangedListener(PinChangedListener l) {
        pinChangedListeners.remove(l);
    }
    
    //TODO: esta transformação de PORTA para PINO deveria ocorrer numa classe
    //      que encapsule o CPU num ARDUINO
    //      Fica para trabalho futuro
    protected abstract void firePinChangedListener(int port, int bit, int value);

    
    public interface PinChangedListener {
        public void pinChanged(int pin, int value);
    }   


    public void addCPUStatusListener(CPUStatusListener l) {
        if( !cpuStatusListeners.contains(l) ) {
            cpuStatusListeners.add(l);
        }
    }
    
    public void removeCPUStatusListener(CPUStatusListener l) {
        cpuStatusListeners.remove(l);
    }
    
    protected void fireCPUStatusListener(boolean running) {
        for(CPUStatusListener l : cpuStatusListeners) {
            l.statusChanged(running);
        }
    }    
    
    public interface CPUStatusListener {
        public void statusChanged(boolean running);
    }   
}
