/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.isec.deis.mis.arduinosimulator.peripherals;

import java.util.ArrayList;
import java.util.List;
import pt.isec.deis.mis.arduinosimulator.CPU;
import pt.isec.deis.mis.arduinosimulator.DataMemory;
import pt.isec.deis.mis.arduinosimulator.Peripheral;
import pt.isec.deis.mis.arduinosimulator.utils.Utils;

/**
 *
 * @author pafgoncalves@ipc.pt
 */
public class UsartImpl implements Peripheral, Usart, DataMemory.DataMemoryChangedListener, DataMemory.MemoryCallback {

    //TODO: se estes endereços forem configurados no construtor
    //      a mesma classe serve para o USART0, USART1, ...
    protected final int UCSRA; //RXCn TXCn UDREn FEn DORn UPEn U2Xn MPCMn
    protected final int UCSRB; //RXCIEn TXCIEn UDRIEn RXENn TXENn UCSZn2 RXB8n TXB8n
    protected final int UCSRC;
    protected final int UBRRL;
    protected final int UBRRH;
    protected final int UDR;
    
    protected final CPU cpu;
    protected boolean txEnabled = false;
    protected boolean txInterrupt = false;
    protected boolean rxEnabled = false;
    protected boolean rxInterrupt = false;
    
    protected List<Integer> buffer = new ArrayList<>();
    
    protected List<UsartListener> listeners = new ArrayList<>();
    
    public UsartImpl(CPU cpu, int ucsraAddr, int ucsrbAddr, int ucsrcAddr, 
            int ubrrlAddr, int ubrrhAddr, int udrAddr) {
        
        this.cpu = cpu;
        
        UCSRA = ucsraAddr;
        UCSRB = ucsrbAddr;
        UCSRC = ucsrcAddr;
        UBRRH = ubrrhAddr;
        UBRRL = ubrrlAddr;
        UDR = udrAddr;
        
//        cpu.getSRAM().addDataMemoryChangedListener(this,"0xC0-0xC6");
        cpu.getSRAM().addDataMemoryChangedListener(this, new int[] {UCSRA,UCSRB,UDR});
        cpu.getSRAM().setMemoryCallback(UDR, this);
    }
    
    @Override
    public void clock() {
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
        //cpu.getSRAM().removeMemoryCallback(UDR);
    }
    
    @Override
    public void reset() {
        //condição de reset (p. 283)
        cpu.getSRAM().set(UCSRA, cpu.getSRAM().get(UCSRA)|0x20, false );
    }

    @Override
    public void dataMemoryChanged(int address) {
        if(address==UCSRA) {
//          System.out.println("UCSR0A := "+Utils.toHex(cpu.getSRAM().get(UCSRA)));
            cpu.getSRAM().set(UCSRA, cpu.getSRAM().get(UCSRA)|0x20, false);
        } else if( address==UCSRC ) {
//          System.out.println("UCSR0C := "+Utils.toHex(cpu.getSRAM().get(UCSRC)));
        } else if( address==UBRRL ) {
//          System.out.println("UBRRL := "+Utils.toHex(cpu.getSRAM().get(UBRRL)));
        } else if( address==UBRRH ) {
//          System.out.println("UBRRH := "+Utils.toHex(cpu.getSRAM().get(UBRRH)));
        } else if( address==UCSRB ) {
            //active/not active
//          System.out.println("UCSR0B := "+Utils.toHex(cpu.getSRAM().get(UCSRB)));
            txEnabled = (cpu.getSRAM().get(UCSRB)&0x08) == 0x08;
            txInterrupt = (cpu.getSRAM().get(UCSRB)&0x40) == 0x40;
            rxEnabled = (cpu.getSRAM().get(UCSRB)&0x10) == 0x10;
            rxInterrupt = (cpu.getSRAM().get(UCSRB)&0x80) == 0x80;
            //System.out.println("txEnabled = "+txEnabled);

            if( !rxEnabled ) {
                //System.out.println("buffer clear");
                buffer.clear();
            }
        } else if( address==UDR ) {
//                System.out.println("data");
            if( txEnabled ) {
//                    System.out.println("UDR0 := "+Utils.toHex(cpu.getSRAM().get(UDR)));
                char c = (char)cpu.getSRAM().get(UDR,true);
                //enviar isto para outro lado e não o STDOUT
                //System.out.print(c);
                synchronized(this) {
                    for(UsartListener l : listeners) {
                        l.onChar(c);
                    }
                }
//               if( c<33 | c>125 ) {
//                  System.out.println("serial: 0x"+Utils.toHex(cpu.getSRAM().get(UDR)));
//               }
                //signal that the buffer is empty
                cpu.getSRAM().set(UCSRA, cpu.getSRAM().get(UCSRA)|0x20, false);
                //o txInterrupt nunca é gerada porque nós deixamos o buffer
                //de transmissão SEMPRE vazio
            }
        }
    }
    
    @Override
    public void write(char c) {
        if( rxEnabled ) {
//            System.out.println("vou enviar "+c);
            buffer.add((int)c);
//            System.out.println(buffer.size());
            //signal that the buffer is not empty
            //a chamada da callback deve limpar esta flag
            cpu.getSRAM().set(UCSRA, cpu.getSRAM().get(UCSRA)|0x80);
            if( rxInterrupt ) {
                cpu.interrupt();
            }
        }
    }

    @Override
    public int readMemoryValue(int address) {
//        System.out.println("a ler "+Utils.toHex(address, 4));
        if( !buffer.isEmpty() ) {
            int c = buffer.remove(0);
//            System.out.println("leu callback "+Utils.toHex(cpu.getPc(),4)+" "+Utils.toHex(c));
//            Utils.printStackTrace();
            if( buffer.isEmpty() ) {
                //clear the RXC flag
                //deve ser a chamada da callback a limpar esta flag para ser limpo apenas depois de ser lido o caracter
                cpu.getSRAM().set(UCSRA, cpu.getSRAM().get(UCSRA)&(~0x80));
//                System.out.println("limpou interrupção: "+Utils.toHex(cpu.getSRAM().get(UCSRA)));
            }
            return c;
        }
        return 0;
    }

    @Override
    public synchronized void addListener(UsartListener l) {
        if( !listeners.contains(l) ) {
            listeners.add(l);
        }
    }

    @Override
    public synchronized void removeListener(UsartListener l) {
        listeners.remove(l);
    }

}
