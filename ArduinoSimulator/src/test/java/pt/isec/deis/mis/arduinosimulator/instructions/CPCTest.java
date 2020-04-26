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
public class CPCTest {
    
    static CPU cpu = null;
    static CPC instance = null;
    
    public CPCTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        cpu = new ATmega328P();
        instance = new CPC();
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
     * Test of execute method, of class CPC.
     */
    @Test
    public void testZero() {
        cpu.getSRAM().getStatusRegisterObj().setCarry(true);
        cpu.getSRAM().getStatusRegisterObj().setZero(true);
        int rdAddr = 0;
        int rrAddr = 1;
        cpu.getSRAM().setRegister(rdAddr, 1);
        cpu.getSRAM().setRegister(rrAddr, 0);
        
        //execute
        instance.execute(cpu, rdAddr, rrAddr);
        
        //assert flags
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getCarry());
        assertEquals(true, cpu.getSRAM().getStatusRegisterObj().getZero());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getNegative());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getOverflow());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getSign());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getHalfCarry());
    }

    /**
     * Test of execute method, of class CPC.
     */
    @Test
    public void testCarry() {
        cpu.getSRAM().getStatusRegisterObj().setCarry(true);
        cpu.getSRAM().getStatusRegisterObj().setZero(true);
        int rdAddr = 0;
        int rrAddr = 1;
        cpu.getSRAM().setRegister(rdAddr, 1);
        cpu.getSRAM().setRegister(rrAddr, 2);
        
        //execute
        instance.execute(cpu, rdAddr, rrAddr);
        
        //assert flags
        assertEquals(true, cpu.getSRAM().getStatusRegisterObj().getCarry());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getZero());
        assertEquals(true, cpu.getSRAM().getStatusRegisterObj().getNegative());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getOverflow());
        assertEquals(true, cpu.getSRAM().getStatusRegisterObj().getSign());
        assertEquals(true, cpu.getSRAM().getStatusRegisterObj().getHalfCarry());
    }
    

    /**
     * Test of execute method, of class CPC.
     */
    @Test
    public void testCarry2() {
        cpu.getSRAM().getStatusRegisterObj().setCarry(true);
        cpu.getSRAM().getStatusRegisterObj().setZero(true);
        int rdAddr = 0;
        int rrAddr = 1;
        cpu.getSRAM().setRegister(rdAddr, 2);
        cpu.getSRAM().setRegister(rrAddr, 1);
        
        //execute
        instance.execute(cpu, rdAddr, rrAddr);
        
        //assert flags
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getCarry());
        assertEquals(true, cpu.getSRAM().getStatusRegisterObj().getZero());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getNegative());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getOverflow());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getSign());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getHalfCarry());
    }    
    
}
