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

/**
 *
 * @author pafgoncalves@ipc.pt
 */
public class ADDTest {
    
    static CPU cpu = null;
    static ADD instance = null;
    
    public ADDTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        cpu = new ATmega328P();
        instance = new ADD();
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
     * Test of execute method, of class ADD.
     */
    @Test
    public void testAdd() {
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
     * Test of execute method, of class ADD.
     */
    @Test
    public void testZero() {
        int rdAddr = 0;
        int rrAddr = 1;
        cpu.getSRAM().setRegister(rdAddr, -1);
        cpu.getSRAM().setRegister(rrAddr, 1);
        
        //execute
        instance.execute(cpu, rdAddr, rrAddr);
        
        //assert result
        assertEquals(0, cpu.getSRAM().getRegister(rdAddr));
        
        //assert flags
        assertEquals(true, cpu.getSRAM().getStatusRegisterObj().getCarry());
        assertEquals(true, cpu.getSRAM().getStatusRegisterObj().getZero());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getNegative());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getOverflow());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getSign());
        assertEquals(true, cpu.getSRAM().getStatusRegisterObj().getHalfCarry());
    }


    /**
     * Test of execute method, of class ADD.
     */
    @Test
    public void testUsesCarry() {
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
     * Test of execute method, of class ADD.
     */
    @Test
    public void testCarry() {
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
    @Test
    public void testCarry2() {
        int rdAddr = 0;
        int rrAddr = 1;
        cpu.getSRAM().setRegister(rdAddr, 128);
        cpu.getSRAM().setRegister(rrAddr, 128);

        //execute
        instance.execute(cpu, rdAddr, rrAddr);
        
        //assert result
        assertEquals(0, cpu.getSRAM().getRegister(rdAddr));
        
        //assert flags
        assertEquals(true, cpu.getSRAM().getStatusRegisterObj().getCarry());
        assertEquals(true, cpu.getSRAM().getStatusRegisterObj().getZero());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getNegative());
        assertEquals(true, cpu.getSRAM().getStatusRegisterObj().getOverflow());
        assertEquals(true, cpu.getSRAM().getStatusRegisterObj().getSign());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getHalfCarry());
    }

    /**
     * Test of execute method, of class ADD.
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
