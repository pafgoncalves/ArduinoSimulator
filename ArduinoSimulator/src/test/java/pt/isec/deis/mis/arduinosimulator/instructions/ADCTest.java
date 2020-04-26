/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.isec.deis.mis.arduinosimulator.instructions;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import pt.isec.deis.mis.arduinosimulator.ATmega328P;
import pt.isec.deis.mis.arduinosimulator.CPU;
import pt.isec.deis.mis.arduinosimulator.DataMemory;

/**
 *
 * @author pafgoncalves@ipc.pt
 */
public class ADCTest {
    
    static CPU cpu = null;
    static ADC instance = null;
    
    public ADCTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        cpu = new ATmega328P();
        cpu.getSRAM().addDataMemoryChangedListener(new DataMemory.DataMemoryChangedListener() {
            @Override
            public void dataMemoryChanged(int address) {
//                System.out.println("> changed "+Integer.toHexString(address)+" "+cpu.getSRAM().get(address));
            }
        }, 0x5F);
        instance = new ADC();
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        cpu.reset();
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of execute method, of class ADC.
     */
    @Test
    public void testAdd() {
        System.out.println("execute");
        int rdAddr = 0;
        int rrAddr = 1;
        cpu.getSRAM().setRegister(rdAddr, 1);
        cpu.getSRAM().setRegister(rrAddr, 1);
        
        //execute
        instance.execute(cpu, rdAddr, rrAddr);
        
        //assert result
        assertEquals(2, cpu.getSRAM().getRegister(rdAddr));
        
        //assert flags
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getCarry());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getZero());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getNegative());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getOverflow());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getSign());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getHalfCarry());
    }

    /**
     * Test of execute method, of class ADC.
     */
    @Test
    public void testZero() {
        System.out.println("execute2");
        int rdAddr = 0;
        int rrAddr = 1;
        cpu.getSRAM().setRegister(rdAddr, -1);
        cpu.getSRAM().setRegister(rrAddr, 1);
//        System.out.println("status: "+Integer.toBinaryString(cpu.getSRAM().getStatusRegister()));
//        System.out.println("zero: "+cpu.getSRAM().getStatusRegisterObj().getZero());

        //execute
        instance.execute(cpu, rdAddr, rrAddr);
        
        //assert result
        assertEquals(0, cpu.getSRAM().getRegister(rdAddr));
//        System.out.println("status: "+Integer.toBinaryString(cpu.getSRAM().getStatusRegister()));
//        System.out.println("zero: "+cpu.getSRAM().getStatusRegisterObj().getZero());
        
        //assert flags
        assertEquals(true, cpu.getSRAM().getStatusRegisterObj().getCarry());
        assertEquals(true, cpu.getSRAM().getStatusRegisterObj().getZero());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getNegative());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getOverflow());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getSign());
        assertEquals(true, cpu.getSRAM().getStatusRegisterObj().getHalfCarry());
    }
    

    /**
     * Test of execute method, of class ADC.
     */
    @Test
    public void testUsesCarry() {
        System.out.println("execute3");
        int rdAddr = 0;
        int rrAddr = 1;
        cpu.getSRAM().getStatusRegisterObj().setCarry(true);
        cpu.getSRAM().setRegister(rdAddr, 1);
        cpu.getSRAM().setRegister(rrAddr, 1);

        //execute
        instance.execute(cpu, rdAddr, rrAddr);
        
        //assert result
        assertEquals(3, cpu.getSRAM().getRegister(rdAddr));
        
        //assert flags
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getCarry());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getZero());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getNegative());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getOverflow());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getSign());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getHalfCarry());
    }
    

    /**
     * Test of execute method, of class ADC.
     */
    @Test
    public void testCarry() {
        System.out.println("execute4");
        int rdAddr = 0;
        int rrAddr = 1;
        cpu.getSRAM().setRegister(rdAddr, 128);
        cpu.getSRAM().setRegister(rrAddr, 130);

        //execute
        instance.execute(cpu, rdAddr, rrAddr);
        
        //assert result
        assertEquals(2, cpu.getSRAM().getRegister(rdAddr));
        
        //assert flags
        assertEquals(true, cpu.getSRAM().getStatusRegisterObj().getCarry());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getZero());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getNegative());
        assertEquals(true, cpu.getSRAM().getStatusRegisterObj().getOverflow());
        assertEquals(true, cpu.getSRAM().getStatusRegisterObj().getSign());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getHalfCarry());
    }

    /**
     * Test of execute method, of class ADC.
     */
    @Test
    public void testHalfCarry() {
        int rdAddr = 0;
        int rrAddr = 1;
        cpu.getSRAM().setRegister(rdAddr, 8);
        cpu.getSRAM().setRegister(rrAddr, 8);

        //execute
        instance.execute(cpu, rdAddr, rrAddr);
        
        //assert result
        assertEquals(16, cpu.getSRAM().getRegister(rdAddr));
        
        //assert flags
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getCarry());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getZero());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getNegative());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getOverflow());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getSign());
        assertEquals(true, cpu.getSRAM().getStatusRegisterObj().getHalfCarry());
    }
    
}
