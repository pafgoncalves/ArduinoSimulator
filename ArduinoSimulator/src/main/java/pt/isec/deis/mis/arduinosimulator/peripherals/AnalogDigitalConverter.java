/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.isec.deis.mis.arduinosimulator.peripherals;

import pt.isec.deis.mis.arduinosimulator.CPU;
import pt.isec.deis.mis.arduinosimulator.DataMemory;
import pt.isec.deis.mis.arduinosimulator.Peripheral;

/**
 *
 * @author pafgoncalves@ipc.pt
 */
public class AnalogDigitalConverter implements Peripheral, DataMemory.DataMemoryChangedListener {

    protected static final int ADMUX = 0x7C;
    protected static final int ADCSRA = 0x7A;
    protected static final int ADCL = 0x78;
    protected static final int ADCH = 0x79;
    protected static final int ADCSRB = 0x7B;
    protected static final int DIDR0 = 0x7E;
    
    protected final CPU cpu;
    
    public AnalogDigitalConverter(CPU cpu) {
        this.cpu = cpu;
        cpu.getSRAM().addDataMemoryChangedListener(this,"0x7A");
    }
    
    @Override
    public void clock() {
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

    @Override
    public void reset() {
    }

    @Override
    public void dataMemoryChanged(int address) {
        switch(address) {
            case ADCSRA:
                if( (cpu.getSRAM().get(ADCSRA)&0xC0) == 0xC0 ) {
                    //comparar com o quê
                    int ref = cpu.getSRAM().get(ADMUX)&0xC0 >> 6;
                    Float vRef = null;
                    switch(ref) {
                        case 0:
                              //portA pin 8 (a porta só tem 8 pinos, considerei o 9º o AREF)
                            vRef = cpu.getInputPinValue(0, 8);
                            break;
                        case 1:
                            vRef = 5.0f;
                            break;
                        case 3:
                            vRef = 1.1f;
                            break;
                    }
                    //origem do valor analógico
                    int inputChannel = cpu.getSRAM().get(ADMUX)&0x0F;
                    //lê o valor no pino/xxx
                    Float vIn = null;
                    if( inputChannel<=7 ) {
                        vIn = cpu.getInputPinValue(0, inputChannel);
                    }
                    int intValue = 0;
                    if( vRef!=null && vRef!=0 && vIn!=null ) {
                        intValue = (int)((vIn*1024)/vRef);
                        if( intValue<0 ) {
                            intValue = 0;
                        } else if( intValue>0x3FF ) {
                            intValue = 0x3FF;
                        }
                    }
                    //escreve nos registos de dados
                    if( (cpu.getSRAM().get(ADMUX)&0x20) == 0x20 ) {
                        //left adjust
                        cpu.getSRAM().set(ADCL, (intValue<<6)&0xC0);
                        cpu.getSRAM().set(ADCH, (intValue>>2)&0xFF);
                    } else {
                        //right adjust
                        cpu.getSRAM().set(ADCL, intValue&0xFF);
                        cpu.getSRAM().set(ADCH, (intValue>>8)&0x03);
                    }
                    //activa a flag de fim de conversão e reset do bit start conversion
                    cpu.getSRAM().set(ADCSRA, cpu.getSRAM().get(ADCSRA)|0x10, false);
                    cpu.getSRAM().set(ADCSRA, cpu.getSRAM().get(ADCSRA)&0xBF, false);
                    //gera interrupção no caso de estar activa
                    if( (cpu.getSRAM().get(ADCSRA)&0x08) == 0x08 ) {
                        cpu.interrupt();
                    }
                }
                break;
        }
    }
    
}
