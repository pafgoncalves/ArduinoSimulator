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
public class Timer0Dummy implements Peripheral, Runnable {
    
    private static final int TIMSK0 = 0x6E;
    private static final int TCNT0 = 0x46;
    private static final int TIFR0 = 0x35;
    private static final int TCCR0A = 0x44;
    private static final int TCCR0B = 0x45;
    private static final int OCR0A = 0x47;
    private static final int OCR0B = 0x48;
    
    private final CPU cpu;

    
    private boolean running = false;
    
    public Timer0Dummy(CPU cpu) {
        this.cpu = cpu;
    }

    @Override
    public void clock() {
    }
    
    @Override
    public void start() {
        Thread t = new Thread(this);
        t.setDaemon(true);
        running = true;
        t.start();
    }
    
    @Override
    public void stop() {
        running = false;
    }

    @Override
    public void reset() {
        cpu.getSRAM().set(TCNT0,1);
    }    
    
    long time = 0;
    public void clock0() {
            //activar a interrupção
            cpu.getSRAM().set(TIFR0, cpu.getSRAM().get(TIFR0)|0x01);
            cpu.interrupt();
    }

    int delay = 900_000;
    @Override
    public void run() {
        int counter = 0;
        long curTime;
        long prevTime = System.nanoTime();
        while(running) {
            try {
                clock0();
                Thread.sleep(0, delay);
                counter++;
                curTime = System.nanoTime();
                delay += 900_000-(curTime-prevTime);
                if( delay<0 ) {
                    delay = 0;
                } else if( delay>999_999 ) {
                    delay = 999_999;
                }
                prevTime = curTime;
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    
}
