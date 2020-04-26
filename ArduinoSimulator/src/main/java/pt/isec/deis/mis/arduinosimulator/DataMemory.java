/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.isec.deis.mis.arduinosimulator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import pt.isec.deis.mis.arduinosimulator.utils.Utils;

/**
 *
 * @author pafgoncalves@ipc.pt
 */
public class DataMemory {
    
    protected final int RAMEND;
    protected final int REGEND;
    protected final int SP;
    protected final int SREG;
    protected final int EIND;
    protected final int RAMPX;
    protected final int RAMPY;
    protected final int RAMPZ;
    protected final int RAMPD;
    
    protected final int[] memory;
    protected final MemoryCallback[] readCallbacks;
    
    protected final List<DataMemoryChangedListener> dataMemoryDiscardableChangedListeners = new ArrayList<>();
    protected final List<DataMemoryChangedListener>[] dataMemoryChangedListeners;
    
    protected final BlockingQueue<Integer> dataMemoryChangedAddresses = new ArrayBlockingQueue<>(10000);
    protected final BlockingQueue<Integer> dataMemoryDiscardableChangedAddresses = new ArrayBlockingQueue<>(10000);
//    protected final BlockingQueue<Integer> dataMemoryChangedAddresses = new LinkedBlockingDeque<>();

    protected Thread discardableChangedAddressesThread = null;
    protected Thread changedAddressesThread = null;
    
    protected boolean hasListeners = false;
    
    protected final StatusRegister statusRegister = new StatusRegister();

    public DataMemory(int ramEnd, int registersEnd, int stackPointerAddr, int statusRegisterAddr) {
        this(ramEnd, registersEnd, stackPointerAddr, statusRegisterAddr, -1, -1, -1, -1, -1);
    }
    
    public DataMemory(int ramEnd, int registersEnd, int stackPointerAddr, 
            int statusRegisterAddr, int eindAddr, int rampXAddr, int rampYAddr,
            int rampZAddr, int rampDAddr) {
        
        RAMEND = ramEnd;
        REGEND = registersEnd;
        SP = stackPointerAddr;
        SREG = statusRegisterAddr;
        
        EIND = eindAddr;
        RAMPX = rampXAddr;
        RAMPY = rampYAddr;
        RAMPZ = rampZAddr;
        RAMPD = rampDAddr;
        
        memory = new int[RAMEND+1];
        readCallbacks = new MemoryCallback[RAMEND+1];
        dataMemoryChangedListeners = new List[memory.length];
        reset();
        startThreads();
    }
    
    public void reset() {
        for(int i=0; i<=REGEND; i++) {
            memory[i] = 0;
        }
        //Não é feito o reset à SRAM, só aos registos
//        for(int i=0x100; i<memory.length; i++) {
//            memory[i] = 0xFF;
//        }
        setStackPointer(RAMEND);
    }
    
    public int size() {
        return memory.length;
    }
    
    public int get(int addr) {
        return get(addr, false);
    }
    
    /**
     * Reads a data memory location.
     * 
     * @param addr Address of the memory to read
     * @param direct If true doesn't call the registred callbacks and reads the value from memory
     * @return The value at the address
     */
    public int get(int addr, boolean direct) {
//        if( addr >= 0xC0 && addr <= 0xC6 ) {
//        if( addr == 0xC0 ) {
//            System.out.println("leu 0x"+Integer.toHexString(addr)+" "+Utils.toHex(memory[addr]&0xFF));
//            System.out.println(""+readCallbacks[addr]);
//        }
//        if( addr >= 0x23 && addr <= 0xDD ) {
//            System.out.println("acesso de leitura a periférico "+addr);
//        }
        //TODO: As leituras dos pinos analógicos, se não estiverem ligados a nada,
        //      devem ter uma leitura alimentada pelo random() do Java.
        //      Isso deve ser feito no conversor ADC e não aqui.
        //      Como saber se a porta está ligada a alguma coisa?
        //      O CPU deve ter algo do género: connectPin(pinAddress, callback);
        //      A callback deverá devolver o valor do pino.
        //      Quando aqui lermos uma porta de entrada, temos de chamar a callback
        //      para sabermos o valor.
        //      Essa callback pode devolver null, se não estiver ligado, ou um valor 
        //      entre 0 e 1 ou 5, para podermos fazer uma análise analógica.
        if( !direct && readCallbacks[addr]!=null ) {
            return readCallbacks[addr].readMemoryValue(addr);
        }
        return memory[addr]&0xFF;
    }

    public void set(int addr, int value) {
        set(addr, value, true);
    }
    
    long last = 0;
    public void set(int addr, int value, boolean fireListener) {
        //TODO: ao escrever 1 nos registos PINx o valor do bit deve ser invertido e não colocado a 1
        //      isto pode-se resolver colocando um listener nos endereços PINx no CPU
        //      como o CPU precisa do valor anterior o fireDataMemoryChanged tem de 
        //      passar o valor anterior por parametro "fireDataMemoryChanged(addr,previous);"
        //      e depois o CPU faz o bit flip e escreve na memoria 
        //      com "set(addr, value, false);" para evitar lançar outra vez o evento
        
        
//        if( addr >= 0x23 && addr <= 0xDD ) {
//            System.out.println("acesso de escrita a periférico "+Utils.toHex(addr)+" "+Utils.toHex(value));
//            Utils.printStackTrace();
//        }
        memory[addr] = value&0xFF;
        
        if( fireListener ) {
            fireDataMemoryChanged(addr);
        }
    }
    
    private int getW(int addr) {
        return (get(addr+1))<<8 | get(addr);
    }
    
    private void setW(int addr, int value) {
        set(addr, value&0xFF);
        set(addr+1, (value>>8)&0xFF);
    }
    
    public int getStackW(int addr) {
        return (get(addr-1))<<8 | get(addr);
    }
    
    public int getStack3(int addr) {
        return (get(addr-2))<<16 | (get(addr-1))<<8 | get(addr);
    }
    
    public void setStackW(int addr, int value) {
        set(addr, value&0xFF);
        set(addr-1, (value>>8)&0xFF);
    }
    
    public void setStack3(int addr, int value) {
        set(addr, value&0xFF);
        set(addr-1, (value>>8)&0xFF);
        set(addr-2, (value>>16)&0xFF);
    }
    
    public int getRegister(int r) {
        if( r>31 ) throw new IllegalArgumentException("Illegal register "+r);
        return get(r);
    }

    public void setRegister(int r, int value) {
        if( r>31 ) throw new IllegalArgumentException("Illegal register "+r);
        set(r,value);
    }
    
    public int getRegisterX() {
        return getW(0x1A);
    }

    public void setRegisterX(int value) {
        setW(0x1A, value);
    }
    
    
    public int getRegisterY() {
        return getW(0x1C);
    }
    
    public void setRegisterY(int value) {
        setW(0x1C, value);
    }
    
    public int getRegisterZ() {
        return getW(0x1E);
    }

    public void setRegisterZ(int value) {
        setW(0x1E, value);
    }
    
    public int getStackPointer() {
        //TODO: esta mascara deve depender do tamanho do PC ou não?
        return getW(SP)&0xFFF;
    }
    
    public void setStackPointer(int value) {
        //TODO: esta mascara deve depender do tamanho do PC ou não?
        setW(SP, value&0xFFF);
    }
    
    public int getRegisterEIND() {
        return EIND!=-1?get(EIND):0;
    }
    
    public void setRegisterEIND(int value) {
        if( EIND!=-1 ) {
            set(EIND, value);
        }
    }
    
    public int getRegisterRAMPX() {
        return RAMPX!=-1?get(RAMPX):0;
    }
    
    public void setRegisterRAMPX(int value) {
        if( RAMPX!=-1 ) {
            set(RAMPX, value);
        }
    }
    
    public int getRegisterRAMPY() {
        return RAMPY!=-1?get(RAMPY):0;
    }
    
    public void setRegisterRAMPY(int value) {
        if( RAMPY!=-1 ) {
            set(RAMPY, value);
        }
    }
    
    public int getRegisterRAMPZ() {
        return RAMPZ!=-1?get(RAMPZ):0;
    }
    
    public void setRegisterRAMPZ(int value) {
        if( RAMPZ!=-1 ) {
            set(RAMPZ, value);
        }
    }
    
    public int getRegisterRAMPD() {
        return RAMPD!=-1?get(RAMPD):0;
    }
    
    public void setRegisterRAMPD(int value) {
        if( RAMPD!=-1 ) {
            set(RAMPD, value);
        }
    }
    
    public int getIO(int addr) {
        if( addr<0 || addr>0x3F ) throw new IllegalArgumentException("Illegal IO "+addr);
        return get(addr+0x20);
    }

    public void setIO(int addr, int value) {
        if( addr<0 || addr>0x3F ) throw new IllegalArgumentException("Illegal IO "+addr);
        set(addr+0x20, value);
    }

    public int getStatusRegister() {
        return get(SREG);
    }

    public void setStatusRegister(int value) {
        set(SREG, value);
    }

    public StatusRegister getStatusRegisterObj() {
//        return new StatusRegister();
        return statusRegister;
    }

    
    public void setMemoryCallback(int address, MemoryCallback mc) {
        readCallbacks[address] = mc;
    }
    
    public void removeMemoryCallback(int address) {
        setMemoryCallback(address, null);
    }
    
    public void addDataMemoryChangedListener(DataMemoryChangedListener listener) {
        if( !dataMemoryDiscardableChangedListeners.contains(listener) ) {
            dataMemoryDiscardableChangedListeners.add(listener);
            hasListeners = true;
        }
    }

    public void addDataMemoryChangedListener(DataMemoryChangedListener listener, int address) {
        List<DataMemoryChangedListener> listeners = dataMemoryChangedListeners[address];
        if( listeners==null ) {
            listeners = new ArrayList<>();
            dataMemoryChangedListeners[address] = listeners;
        }
        if( !listeners.contains(listener) ) {
            listeners.add(listener);
        }
    }

    /**
     * Adds a DataMemoryChangedListener for specific addresses.
     * 
     * @param listener
     * @param addressesList The list of addresses split by comma "," and with possible ranges "5,40,0x50-0x55".
     */
    public void addDataMemoryChangedListener(DataMemoryChangedListener listener, String addressesList) {
        String[] addressesArray = addressesList.split(",");
        for(String address : addressesArray) {
            if( address.contains("-") ) {
                String[] limits = address.split("-",2);
                for(int a=Utils.parseInt(limits[0]); a<=Utils.parseInt(limits[1]); a++) {
                    addDataMemoryChangedListener(listener, a);
                }
            } else {
                addDataMemoryChangedListener(listener, Utils.parseInt(address));
            }
        }
    }
    

    /**
     * Adds a DataMemoryChangedListener for specific addresses.
     * 
     * @param listener
     * @param addresses Array of addresses to listen for.
     */
    public void addDataMemoryChangedListener(DataMemoryChangedListener listener, int[] addresses) {
        for(int address : addresses) {
            addDataMemoryChangedListener(listener, address);
        }
    }
    
    public void removeDataMemoryChangedListener(DataMemoryChangedListener listener) {
        dataMemoryDiscardableChangedListeners.remove(listener);
        if( dataMemoryDiscardableChangedListeners.isEmpty() ) {
            hasListeners = false;
        }
        for(List<DataMemoryChangedListener> listeners : dataMemoryChangedListeners) {
            if( listeners!=null ) {
                listeners.remove(listener);
            }
        }
    }
    
    private void fireDataMemoryChanged(int address) {
        if( hasListeners /*!dataMemoryDiscardableChangedListeners.isEmpty()*/ ) {
            boolean offer = dataMemoryDiscardableChangedAddresses.offer(address);
            if( !offer ) {
//              System.out.println("lista de endereços modificados cheia!");
                dataMemoryDiscardableChangedAddresses.clear();
                dataMemoryDiscardableChangedAddresses.offer(address);
            }
        }
//        if( dataMemoryChangedListeners[address]!=null ) {
//            offer = dataMemoryChangedAddresses.offer(address);
//            if( !offer ) {
//                System.out.println("lista de endereços modificados cheia!");
//                try {
//                    dataMemoryChangedAddresses.put(address);
//                } catch(Exception e) {
//                    e.printStackTrace(System.err);
//                }
//            }
//        }
        //para a porta série funcionar isto tem de ser síncrono
        List<DataMemoryChangedListener> listeners = dataMemoryChangedListeners[address];
        if( listeners!=null ) {
            for(DataMemoryChangedListener l : listeners) {
                l.dataMemoryChanged(address);
            }
        }


    }
/*
    //correr isto numa thread à parte e chamado no final do execute() do CPU
    public void fireDataMemoryChanged() {
        synchronized( this ) {
            notify();
        }
    }
*/
    
    
    private void startThreads() {
        discardableChangedAddressesThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while( true ) {
                    try {
                        int address = dataMemoryDiscardableChangedAddresses.take();
                        for(DataMemoryChangedListener l : dataMemoryDiscardableChangedListeners) {
                            l.dataMemoryChanged(address);
                        }
                    } catch(InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        },"DiscardableChangedAddressesThread");
        discardableChangedAddressesThread.setDaemon(true);
        discardableChangedAddressesThread.start();
/*
        changedAddressesThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while( true ) {
                    try {
                        int address = dataMemoryChangedAddresses.take();
                        List<DataMemoryChangedListener> listeners = dataMemoryChangedListeners[address];
                        if( listeners!=null ) {
                            for(DataMemoryChangedListener l : listeners) {
                                l.dataMemoryChanged(address);
                            }
                        }
                    } catch(InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        },"changedAddressesThread");
        changedAddressesThread.setDaemon(true);
        changedAddressesThread.start();
*/        
    }

    public class StatusRegister {
        
        public boolean getGlobalInterruptEnable() {
            return (getStatusRegister()&0x80)!=0;
        }
        
        public void setGlobalInterruptEnable(boolean value) {
            if( value ) {
                setStatusRegister(getStatusRegister()|0x80);
            } else {
                setStatusRegister(getStatusRegister()&0x7F);
            }
        }
        
        public boolean getCopyStorage() {
            return (getStatusRegister()&0x40)!=0;
        }
        
        public void setCopyStorage(boolean value) {
            if( value ) {
                setStatusRegister(getStatusRegister()|0x40);
            } else {
                setStatusRegister(getStatusRegister()&0xBF);
            }
        }
        
        public boolean getHalfCarry() {
            return (getStatusRegister()&0x20)!=0;
        }
        
        public void setHalfCarry(boolean value) {
            if( value ) {
                setStatusRegister(getStatusRegister()|0x20);
            } else {
                setStatusRegister(getStatusRegister()&0xDF);
            }
        }

        public boolean getSign() {
            return (getStatusRegister()&0x10)!=0;
        }
        
        public void setSign(boolean value) {
            if( value ) {
                setStatusRegister(getStatusRegister()|0x10);
            } else {
                setStatusRegister(getStatusRegister()&0xEF);
            }
        }

        public boolean getOverflow() {
            return (getStatusRegister()&0x8)!=0;
        }
        
        public void setOverflow(boolean value) {
            if( value ) {
                setStatusRegister(getStatusRegister()|0x8);
            } else {
                setStatusRegister(getStatusRegister()&0xF7);
            }
        }

        public boolean getNegative() {
            return (getStatusRegister()&0x4)!=0;
        }
        
        public void setNegative(boolean value) {
            if( value ) {
                setStatusRegister(getStatusRegister()|0x4);
            } else {
                setStatusRegister(getStatusRegister()&0xFB);
            }
        }

        public boolean getZero() {
            return (getStatusRegister()&0x2)!=0;
        }
        
        public void setZero(boolean value) {
            if( value ) {
                setStatusRegister(getStatusRegister()|0x2);
            } else {
                setStatusRegister(getStatusRegister()&0xFD);
            }
        }
        
        public boolean getCarry() {
            return (getStatusRegister()&0x1)!=0;
        }
        
        public void setCarry(boolean value) {
            if( value ) {
                setStatusRegister(getStatusRegister()|0x1);
            } else {
                setStatusRegister(getStatusRegister()&0xFE);
            }
        }
        
    }
    
    public void shutdown() {
        if( discardableChangedAddressesThread != null ) {
            System.out.println("stopping thread "+discardableChangedAddressesThread.getName());
            discardableChangedAddressesThread.interrupt();
            try {
                discardableChangedAddressesThread.join();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        if( changedAddressesThread != null ) {
            System.out.println("stopping thread "+changedAddressesThread.getName());
            changedAddressesThread.interrupt();
            try {
                changedAddressesThread.join();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    
    public interface DataMemoryChangedListener {
        public void dataMemoryChanged(int address);
    }
    
    public interface MemoryCallback {
        public int readMemoryValue(int address);
    }
}
