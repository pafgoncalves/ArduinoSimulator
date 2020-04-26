/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.isec.deis.mis.arduinosimulator.peripherals;

import pt.isec.deis.mis.arduinosimulator.CPU;
import pt.isec.deis.mis.arduinosimulator.Peripheral;

/**
 *
 * @author pafgoncalves@ipc.pt
 */
public class Timer0 implements Peripheral {
    
    private static final int TIMSK0 = 0x6E;
    private static final int TCNT0 = 0x46;
    private static final int TIFR0 = 0x35;
    private static final int TCCR0A = 0x44;
    private static final int TCCR0B = 0x45;
    private static final int OCR0A = 0x47;
    private static final int OCR0B = 0x48;
    
    private final CPU cpu;

    private int timer0Counter = 0;
    
    
    public Timer0(CPU cpu) {
        this.cpu = cpu;
    }

    @Override
    public void clock() {
        clock0();
    }
    
    @Override
    public void start() {
    }
    
    @Override
    public void stop() {
    }
    
    public void clock0() {
        //seleccionar a source do relógio
        int clockSource = cpu.getSRAM().get(TCCR0A) & 0x07;
        if( clockSource != 0 ) {
            
            int compare = -1;
            switch(clockSource) {
                case 1:     // clk/1
                    compare = 1;
                    break;
                case 2:     // clk/8
                    compare = 8;
                    break;
                case 3:     // clk/64
                    compare = 64;
                    break;
                case 4:     // clk/256
                    compare = 256;
                    break;
                case 5:     // clk/1024
                    compare = 1024;
                    break;
                case 6:     // external falling edge
                    break;
                case 7:     // external rising edge
                    break;
            }
            
            if( timer0Counter==compare ) {
                timer0Counter = 0;
                cpu.getSRAM().set(TCNT0, cpu.getSRAM().get(TCNT0)+1);
//                cpu.getSRAM().set(TCNT0, 0xFF);

                //se as interrupções estiverem activas && e o contador fez overflow
                //TODO: verificar se o máximo não pode estar em outro registo
                if( (cpu.getSRAM().get(TIMSK0)&0x1)==0x01 && cpu.getSRAM().get(TCNT0)==0xFF ) {
                    //activar a interrupção
                    cpu.getSRAM().set(TIFR0, cpu.getSRAM().get(TIFR0)|0x01);
                    cpu.interrupt();
                }
            }
            
            timer0Counter++;
        }
        
    }


    @Override
    public void reset() {
    }
    
}
