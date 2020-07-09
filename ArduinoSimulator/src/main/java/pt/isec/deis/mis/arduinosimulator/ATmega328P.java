/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.isec.deis.mis.arduinosimulator;

import java.util.HashMap;
import java.util.Map;
import pt.isec.deis.mis.arduinosimulator.peripherals.AnalogDigitalConverter;
import pt.isec.deis.mis.arduinosimulator.peripherals.Timer0Dummy;
import pt.isec.deis.mis.arduinosimulator.peripherals.UsartImpl;

import static pt.isec.deis.mis.arduinosimulator.ATmega328P.Register.*;
import pt.isec.deis.mis.arduinosimulator.utils.Utils;

/**
 *
 * @author pafgoncalves@ipc.pt
 */
public class ATmega328P extends CPU {

    private static final int ENDRAM = 0x8FF;
    private static final int ENDREG = 0xFF;
    private static final int ENDFLASH = 0x3FFF;

    
    
    protected Float portAREFInput = null;
    
    protected Float[] portBInputs = new Float[6];
    protected int portBInputValue = 0;
    protected Float[] portCInputs = new Float[6];
    protected int portCInputValue = 0;
    protected Float[] portDInputs = new Float[8];
    protected int portDInputValue = 0;
    
    
    
    
    public ATmega328P() {
        super(
            InstructionSetType.AVR,
            new DataMemory(ENDRAM, ENDREG, SPL.getAddress(), SREG.getAddress()),
            new ProgramMemory(ENDFLASH),
            ENDFLASH
        );

        addPeripherals();
    }

    private void addPeripherals() {
        Peripheral timer0 = new Timer0Dummy(this);
        timer0.reset();
        peripherals.add(timer0);

        Peripheral usart0 = new UsartImpl(this, 
                UCSR0A.getAddress(), UCSR0B.getAddress(), UCSR0C.getAddress(), 
                UBRR0L.getAddress(), UBRR0H.getAddress(), UDR0.getAddress()
        );
        usart0.reset();
        peripherals.add(usart0);

        Peripheral adc = new AnalogDigitalConverter(this);
        adc.reset();
        peripherals.add(adc);

        //PORTS
        sram.setMemoryCallback(PINB.getAddress(), new DataMemory.MemoryCallback() {
            @Override
            public int readMemoryValue(int address) {
                return portBInputValue;
            }
        });
        sram.setMemoryCallback(PINC.getAddress(), new DataMemory.MemoryCallback() {
            @Override
            public int readMemoryValue(int address) {
                return portCInputValue;
            }
        });
        sram.setMemoryCallback(PIND.getAddress(), new DataMemory.MemoryCallback() {
            @Override
            public int readMemoryValue(int address) {
                return portDInputValue;
            }
        });

        sram.addDataMemoryChangedListener(new DataMemory.DataMemoryChangedListener() {
            int portBOutputValue = 0;
            int portCOutputValue = 0;
            int portDOutputValue = 0;

            @Override
            public void dataMemoryChanged(int address) {

                switch( Register.valueOf(address) ) {
                    case PORTB: {    //PORTB
                        int value = sram.get(address);
                        int dir = sram.get(DDRB.getAddress());   //DDRB
                        for( int i = 0; i < 6; i++ ) {
                            int mask = 0x1 << i;
                            int bitValue = (value & mask);
                            int bitDir = (dir & mask);
                            //testar se está como output
                            //      DDxn   1 => output
                            if( bitDir != 0 && bitValue != (portBOutputValue & mask) ) {
                                firePinChangedListener(1, i, bitValue >> i);
                            }
                        }
                        portBOutputValue = value;
                    }
                    break;

                    case PORTC: {    //PORTC
                        int value = sram.get(address);
                        int dir = sram.get(DDRC.getAddress());   //DDRC
                        for( int i = 0; i < 6; i++ ) {
                            int mask = 0x1 << i;
                            int bitValue = (value & mask);
                            int bitDir = (dir & mask);
                            if( bitDir != 0 && bitValue != (portCOutputValue & mask) ) {
                                firePinChangedListener(2, i, bitValue >> i);
                            }
                        }
                        portCOutputValue = value;
                    }
                    break;

                    case PORTD: {    //PORTD
                        int value = sram.get(address);
                        int dir = sram.get(DDRD.getAddress());   //DDRD
                        for( int i = 0; i < 8; i++ ) {
                            int mask = 0x1 << i;
                            int bitValue = (value & mask);
                            int bitDir = (dir & mask);
                            if( bitDir != 0 && bitValue != (portDOutputValue & mask) ) {
                                firePinChangedListener(3, i, bitValue >> i);
                            }
                        }
                        portDOutputValue = value;
                    }
                    break;
                }

            }
        }, new int[] {PORTB.getAddress(), PORTC.getAddress(), PORTD.getAddress()});
    }

    @Override
    public String getRegisterName(int addr) {
        Register r = Register.valueOf(addr);
        return r==null?Utils.toHex(addr):r.toString();
    }

    /**
     * Lê os valores analógicos dos pinos
     * Só existem valores analógicos na porta A (mapeada nos pinos digitais da porta C) no Arduino
     */
    @Override
    public Float getInputPinValue(int port, int pin) {
        if( port==0 ) { //port A
            if( pin==8 ) {  //AREF
                return portAREFInput==null?5.0f:portAREFInput;
            }
            //a porta A e C estão nos mesmos pinos sendo a A a porta analógica e a C a digital
            if( pin>=0 && pin<portCInputs.length ) {
                return portCInputs[pin]==null?(random.nextFloat()*5.0f):portCInputs[pin];
            }
        }
        return null;
    }
    
    
    @Override
    public void setInputPinValue(int pin, Float value) {
//        System.out.println("setInputPinValue: "+pin+" "+value);
        
        //portBInputs deve ser um array em que cada bit tem a possibilidade de ser um float entre 0 e 5 ou null.
        //Aqui deve-se construir um inteiro em que os bits a null serão repostos pelo valor default da porta.
        //Por exemplo, no caso de um bit ser null e estiver activo o pull-up deve ser transformado em 5, se
        //não tiver pull-up deve ser um valor random entre 0 e 5.
        //Os valores devem ser transformados em true ou false conforme o valor analogico seja considerado.
        //Por exemplo, a datasheet do ATmega328p define LOW: -0.5 a 0.3*Vcc, HIGH: 0.6Vcc a Vcc+0.5 
        //o que implica LOW (bit a zero): 0-1,5 e HIGH (bit a um): 3-5

        boolean pud = (sram.get(MCUCR.getAddress(), true)&0x10)!=0;   //0x55 => MCUCR
//        System.out.println("PUD: "+pud);
        
        if( pin>=8 && pin<=13 ) {   //PORTB
            pin -= 8;
            
            int mask = 1<<pin;
            boolean previousValue = (portBInputValue&mask)==mask;
            portBInputs[pin] = value;

            if( portBInputs[pin]==null ) {
                //se fica a 0 ou 1 depende do pull-up
                //- PUD bit in MCUCR disables all pull-ups
                //- só deve ser considerado se a porta estiver configurada como entrada?
                //- pull ups => ({DDxn, PORTxn} = 0b01)
                //                DDxn   1 => output
                //                  PORTxn 1 => saida high
                //                  PORTxn 0 => saida low
                //                DDxn   0 => input
                //                  PORTxn 1 => pull-up
                //                  PORTxn 0 => pull-up desactivado
                //TODO: testar no harware real o que acontece quando se lê o PINxn e está configurado como saida
                if( !pud && (sram.get(PORTB.getAddress(), true)&mask)!=0 ) {  //0x25 => PORTB
//                    System.out.println("pull-up "+pin);
                    portBInputValue |= mask;
                } else {
                    //se não temos o pull-up activo a entrada deve ser random
                    if( random.nextBoolean() ) {
//                        System.out.println("RANDOM HIGH "+pin);
                        portBInputValue |= mask;
                    } else {
//                        System.out.println("RANDOM LOW "+pin);
                        portBInputValue &= ~mask;
                    }
                }
            //na datasheet do ATmega328p é considerado uma entrada é considerada negativa para 
            //    in < 1,5
            //e positiva para 
            //    in > 3
            //como existe um intervalo indefinido entre 1,5 e 3 optou-se por fixar 
            //a separação entre negativo e positivo no meio do intervalo (2,25)
            } else if( portBInputs[pin]<2.25 ) {
//                System.out.println("LOW "+pin);
                portBInputValue &= ~mask;
            } else {
//                System.out.println("HIGH "+pin);
                portBInputValue |= mask;
            }

            //PCINT[14-8]
            boolean currentValue = (portBInputValue&mask)==mask;
            if( currentValue!=previousValue ) {
                if( sram.getStatusRegisterObj().getGlobalInterruptEnable() && (sram.get(PCICR.getAddress())&0x01) != 0 ) {
                    if( (sram.get(PCMSK0.getAddress())&mask) != 0 ) {
                        sram.set(PCIFR.getAddress(), sram.get(PCIFR.getAddress())|0x01);
                        interrupt();
                    }
                }
            }
            
        } else if( pin>=14 && pin<=19 ) {   //PORTC
            pin -= 14;
            int mask = 1<<pin;
            boolean previousValue = (portCInputValue&mask)==mask;
            portCInputs[pin] = value;
            if( portCInputs[pin]==null ) {
                if( !pud && (sram.get(PORTC.getAddress(), true)&mask)!=0 ) {  //0x28 => PORTC
                    portCInputValue |= mask;
                } else {
                    if( random.nextBoolean() ) {
                        portCInputValue |= mask;
                    } else {
                        portCInputValue &= ~mask;
                    }
                }
            } else if( portCInputs[pin]<2.25 ) {
                portCInputValue &= ~mask;
            } else {
                portCInputValue |= mask;
            }
            
            //PCINT[14-8]
            boolean currentValue = (portCInputValue&mask)==mask;
            if( currentValue!=previousValue ) {
                if( sram.getStatusRegisterObj().getGlobalInterruptEnable() && (sram.get(PCICR.getAddress())&0x02) != 0 ) {
                    if( (sram.get(PCMSK1.getAddress())&mask) != 0 ) {
                        sram.set(PCIFR.getAddress(), sram.get(PCIFR.getAddress())|0x02);
                        interrupt();
                    }
                }
            }
            
        } else if( pin>=0 && pin<=7 ) {   //PORTD
            int mask = 1<<pin;
            
            boolean previousValue = (portDInputValue&mask)==mask;
            
            portDInputs[pin] = value;
            if( portDInputs[pin]==null ) {
                if( !pud && (sram.get(PORTD.getAddress(), true)&mask)!=0 ) {  //0x2B => PORTD
                    portDInputValue |= mask;
                } else {
                    if( random.nextBoolean() ) {
                        portDInputValue |= mask;
                    } else {
                        portDInputValue &= ~mask;
                    }
                }
            } else if( portDInputs[pin]<2.25 ) {
                portDInputValue &= ~mask;
            } else {
                portDInputValue |= mask;
            }
            
            boolean currentValue = (portDInputValue&mask)==mask;
            //PORTD2 => INT0
            //PORTD3 => INT1
            if( pin==2||pin==3 ) {
                int intMask = 1<<(pin-2);
                if( sram.getStatusRegisterObj().getGlobalInterruptEnable() && (sram.get(EIMSK.getAddress())&intMask) != 0 ) {
                    //testar o tipo de mudança de nível
                    int tipo = sram.get(EICRA.getAddress());
                    if( pin==2 ) {
                        tipo &= 0x03;
                    } else if( pin==3 ) {
                        tipo = (tipo>>2) & 0x03;
                    }
                    boolean occurred = false;
                    switch(tipo) {
                        case 0:
                            if( currentValue==false ) {
                                occurred = true;
                            }
                            break;
                        case 1:
                            if( currentValue!=previousValue ) {
                                occurred = true;
                            }
                            break;
                        case 2:
                            if( currentValue==false && previousValue==true ) {
                                occurred = true;
                            }
                            break;
                        case 3:
                            if( currentValue==true && previousValue==false ) {
                                occurred = true;
                            }
                            break;
                    }
                    if( occurred ) {
                        sram.set(EIFR.getAddress(), sram.get(EIFR.getAddress())|intMask);
                        interrupt();
                    }
                }
            }
            
            //PCINT[23-16]
            if( currentValue!=previousValue ) {
                if( sram.getStatusRegisterObj().getGlobalInterruptEnable() && (sram.get(PCICR.getAddress())&0x04) != 0 ) {
                    if( (sram.get(PCMSK2.getAddress())&mask) != 0 ) {
                        sram.set(PCIFR.getAddress(), sram.get(PCIFR.getAddress())|0x04);
                        interrupt();
                    }
                }
            }

        } else if( pin==21 ) {  //AREF
            portAREFInput = value;
        }
    }
    
    
    @Override
    protected boolean checkInterrupt() {
        //verificar todas as interrupções por ordem
        //a flag global interrup enable é testada antes de chamar este método
        
        //INT 1: reset
        //       => 0x00
        
        //INT 2: External Interrupt Request 0
        //       => 0x02
        if( (sram.get(EIMSK.getAddress())&0x01)==0x01 && (sram.get(EIFR.getAddress())&0x01)==0x01 ) {
            //limpar a interrupção
            sram.set(EIFR.getAddress(), sram.get(EIFR.getAddress())&(~0x01));
            setPc(0x02);
            return true;
        }
        
        //INT 3: External Interrupt Request 1
        //       => 0x04
        if( (sram.get(EIMSK.getAddress())&0x02)==0x02 && (sram.get(EIFR.getAddress())&0x02)==0x02 ) {
            //limpar a interrupção
            sram.set(EIFR.getAddress(), sram.get(EIFR.getAddress())&(~0x02));
            setPc(0x04);
            return true;
        }
        
        //INT 4: Pin Change Interrupt Request 0
        //       => 0x06
        if( (sram.get(PCICR.getAddress())&0x01)==0x01 && (sram.get(PCIFR.getAddress())&0x01)==0x01 ) {
            //limpar a interrupção
            sram.set(PCIFR.getAddress(), sram.get(PCIFR.getAddress())&(~0x01));
            setPc(0x06);
            return true;
        }
        
        //INT 5: Pin Change Interrupt Request 1
        //       => 0x08
        if( (sram.get(PCICR.getAddress())&0x02)==0x02 && (sram.get(PCIFR.getAddress())&0x02)==0x02 ) {
            //limpar a interrupção
            sram.set(PCIFR.getAddress(), sram.get(PCIFR.getAddress())&(~0x02));
            setPc(0x08);
            return true;
        }
        
        //INT 6: Pin Change Interrupt Request 2
        //       => 0x0A
        if( (sram.get(PCICR.getAddress())&0x04)==0x04 && (sram.get(PCIFR.getAddress())&0x04)==0x04 ) {
            //limpar a interrupção
            sram.set(PCIFR.getAddress(), sram.get(PCIFR.getAddress())&(~0x04));
            setPc(0x0A);
            return true;
        }
        
        //INT 17: timer0 interrupt
        //verificar se a interrupção do timer está activa && o overflow ocorreu
        if( (sram.get(TIMSK0.getAddress())&0x1)==0x01 && (sram.get(TIFR0.getAddress())&0x01)==0x01 ) {
            //limpar a interrupção (p. 157)
            sram.set(TIFR0.getAddress(), sram.get(TIFR0.getAddress())&(~0x01));

            //o vector de interrupção depende do processador
            setPc(0x20);

//                if( true ) throw new IllegalAccessException();
//                System.out.print("Interrupt: n. ints "+(interruptCount++)+" ciclos "+(cyclesCount-lastInterrupt)+" ciclos total "+cyclesCount);
//                lastInterrupt = cyclesCount;
//                long now = System.nanoTime();
//                System.out.println(" time: "+(now-timeNanos));
//                timeNanos = now;
            return true;
        }

        //INT 19: usart0 interrupt RX
        //verificar se a interrupção está activa && o buffer está preenchido
        if( (sram.get(UCSR0B.getAddress())&0x80)==0x80 && (sram.get(UCSR0A.getAddress())&0x80)==0x80 ) {

            //o vector de interrupção depende do processador
            setPc(0x24);
            //System.out.println("Interrupção serie");
            return true;
        }

        //INT 20: usart0 interrupt UDRE
        //verificar se a interrupção está activa && o buffer está preenchido
        if( (sram.get(UCSR0B.getAddress())&0x20)==0x20 && (sram.get(UCSR0A.getAddress())&0x20)==0x20 ) {

            //o vector de interrupção depende do processador
            setPc(0x26);
            //System.out.println("Interrupção serie");
            return true;
        }
        
        //INT 22: AnalogDigitalConverter interrupt
        //verificar se a interrupção está activa && a conversão está completa
        if( (sram.get(ADCSRA.getAddress())&0x08)==0x08 && (sram.get(ADCSRA.getAddress())&0x10)==0x10 ) {

            //o vector de interrupção depende do processador
            setPc(0x2A);
            //System.out.println("Interrupção AnalogDigitalConverter");
            return true;
        }
        
        return false;
    }
    
    

    @Override
    public void reset() {
        super.reset();

        for(int i=0; i<portBInputs.length; i++) {
            portBInputs[i] = null;
        }
        for(int i=0; i<portCInputs.length; i++) {
            portCInputs[i] = null;
        }
        for(int i=0; i<portDInputs.length; i++) {
            portDInputs[i] = null;
        }
    }
       
    
    @Override
    protected Runnable getPullUpRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                while( running ) {
                    try {
                        //TODO: tentar melhorar isto
                        //      só faz sentido variar os pinos se os mesmos estiverem a ser lidos pelo firmware
                        //      o problema é descobrir quais é que estão a ser lidos
                        //      talvez seja possível colocar um hook na leitura do porto e só gerar um valor random quando este for lido
                
                        boolean pud = (sram.get(MCUCR.getAddress(), true)&0x10)!=0;   //0x55 => MCUCR
                
                        for(int i=0; i<portBInputs.length; i++) {
                            int mask = 1<<i;
                            if( portBInputs[i]==null ) {
                                if( !pud && (sram.get(DDRB.getAddress(),true)&mask)==0 && (sram.get(PORTB.getAddress(), true)&mask)!=0 ) {
                                    setInputPinValue(i+8, null);
                                }
                            }
                        }
                        for(int i=0; i<portCInputs.length; i++) {
                            int mask = 1<<i;
                            if( portCInputs[i]==null ) {
                                if( !pud && (sram.get(DDRC.getAddress(),true)&mask)==0 && (sram.get(PORTC.getAddress(), true)&mask)!=0 ) {
                                    setInputPinValue(i+14, null);
                                }
                            }
                        }
                        for(int i=0; i<portDInputs.length; i++) {
                            int mask = 1<<i;
                            if( portDInputs[i]==null ) {
                                if( (sram.get(DDRD.getAddress(),true)&mask)==0 && 
                                        (
                                            (pud && (sram.get(PORTD.getAddress(), true)&mask)!=0) || 
                                            (sram.get(PORTD.getAddress(), true)&mask)==0 )
                                        ) {
                                    setInputPinValue(i+0, null);
                                }
                            }
                        }
                        //as operações anteriores demoram cerca de 10 micro-segundos
                        //podemos dormir 100 mili-segundos
                        Thread.sleep(100);
                    } catch(InterruptedException e) {
                    }
                }
            }
        };
    }

    
    @Override
    protected void firePinChangedListener(int port, int bit, int value) {
        //TODO: esta transformação de PORTA para PINO deveria ocorrer numa classe
        //      que encapsule o CPU num ARDUINO
        //      Fica para trabalho futuro
        int pin = -1;
        switch(port) {
            case 0: //A
                break;
            case 1: //B
                pin = bit + 8;
                break;
            case 2: //C
                pin = bit + 14;
                break;
            case 3: //D
                pin = bit;
                break;
        }
        for(PinChangedListener l : pinChangedListeners) {
            l.pinChanged(pin, value);
        }
    }  
    
    
    public enum Register {
        PINB(0x23),
        DDRB(0x24),
        PORTB(0x25),
        PINC(0x26),
        DDRC(0x27),
        PORTC(0x28),
        PIND(0x29),
        DDRD(0x2A),
        PORTD(0x2B),
        /*
        ATmega328PB
        PINE(0x2C),
        DDRE(0x2D),
        PORTE(0x2E),
        */
        TIFR0(0x35),
        TIFR1(0x36),
        TIFR2(0x37),
        /*
        ATmega328PB
        TIFR3(0x38),
        TIFR4(0x39),
        */
        PCIFR(0x3B),
        EIFR(0x3C),
        EIMSK(0x3D),
        GPIOR0(0x3E),
        EECR(0x3F),
        EEDR(0x40),
        EEARL(0x41),
        EEARH(0x42),
        GTCCR(0x43),
        TCCR0A(0x44),
        TCCR0B(0x45),
        TCNT0(0x46),
        OCR0A(0x47),
        OCR0B(0x48),
        GPIOR1(0x4A),
        GPIOR2(0x4B),
        SPCR0(0x4C),
        SPSR0(0x4D),
        SPDR0(0x4E),
        ACSR(0x50),
        DWDR(0x51),
        SMCR(0x53),
        MCUSR(0x54),
        MCUCR(0x55),
        SPMCSR(0x57),
        SPL(0x5D),
        SPH(0x5E),
        SREG(0x5F),
        WDTCSR(0x60),
        CLKPR(0x61),
        /*
        ATmega328PB
        XFDCSR(0x62),
        */
        PRR0(0x64),
        /*
        ATmega328PB
        PRR1(0x65),
        */
        OSCCAL(0x66),
        PCICR(0x68),
        EICRA(0x69),
        PCMSK0(0x6B),
        PCMSK1(0x6C),
        PCMSK2(0x6D),
        TIMSK0(0x6E),
        TIMSK1(0x6F),
        TIMSK2(0x70),
        /*
        ATmega328PB
        TIMSK3(0x71),
        TIMSK4(0x72),
        PCMSK3(0x73),
        */
        ADCL(0x78),
        ADCH(0x79),
        ADCSRA(0x7A),
        ADCSRB(0x7B),
        ADMUX(0x7C),
        DIDR0(0x7E),
        DIDR1(0x7F),
        TCCR1A(0x80),
        TCCR1B(0x81),
        TCCR1C(0x82),
        TCNT1L(0x84),
        TCNT1H(0x85),
        ICR1L(0x86),
        ICR1H(0x87),
        OCR1AL(0x88),
        OCR1AH(0x89),
        OCR1BL(0x8A),
        OCR1BH(0x8B),
        /*
        ATmega328PB
        TCCR3A(0x90),
        TCCR3B(0x91),
        TCCR3C(0x92),
        TCNT3L(0x94),
        TCNT3H(0x95),
        ICR3L(0x96),
        ICR3H(0x97),
        OCR3AL(0x98),
        OCR3AH(0x99),
        OCR3BL(0x9A),
        OCR3BH(0x9B),
        TCCR4A(0xA0),
        TCCR4B(0xA1),
        TCCR4C(0xA2),
        TCNT4L(0xA4),
        TCNT4H(0xA5),
        ICR4L(0xA6),
        ICR4H(0xA7),
        OCR4AL(0xA8),
        OCR4AH(0xA9),
        OCR4BL(0xAA),
        OCR4BH(0xAB),
        SPCR1(0xAC),
        SPSR1(0xAD),
        SPDR1(0xAE),
        */
        TCCR2A(0xB0),
        TCCR2B(0xB1),
        TCNT2(0xB2),
        OCR2A(0xB3),
        OCR2B(0xB4),
        ASSR(0xB6),
        TWBR0(0xB8),
        TWSR0(0xB9),
        TWAR0(0xBA),
        TWDR0(0xBB),
        TWCR0(0xBC),
        TWAMR0(0xBD),
        UCSR0A(0xC0),
        UCSR0B(0xC1),
        UCSR0C(0xC2),
        UBRR0L(0xC4),
        UBRR0H(0xC5),
        UDR0(0xC6);
        /*
        ATmega328PB
        UDR1(0xC7),
        UCSR1A(0xC8),
        UCSR1B(0xC9),
        UCSR1C(0xCA),
        UBRR1L(0xCC),
        UBRR1H(0xCD),
        TWBR1(0xD8),
        TWSR1(0xD9),
        TWAR1(0xDA),
        TWDR1(0xDB),
        TWCR1(0xDC),
        TWAMR1(0xDD);
        */
        
        private final int address;

        private static final Map<Integer, Register> lookup = new HashMap<>();

        static {
            for( Register r : Register.values() ) {
                lookup.put(r.address, r);
            }
        }

        private Register(int address) {
            this.address = address;
        }

        public int getAddress() {
            return address;
        }

        public static Register valueOf(int address) {
            return lookup.get(address);
        }
    }
}
