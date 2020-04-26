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
public class BSETTest {
    
    static CPU cpu = null;
    static BSET instance = null;

    public BSETTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        cpu = new ATmega328P();
        instance = new BSET();
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
     * Test of execute method, of class BSET.
     */
    @Test
    public void testCarry() {
        //execute
        instance.execute(cpu, 0, 0);
        
        //assert flags
        assertEquals(true, cpu.getSRAM().getStatusRegisterObj().getCarry());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getZero());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getNegative());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getOverflow());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getSign());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getHalfCarry());
    }  

    /**
     * Test of execute method, of class BSET.
     */
    @Test
    public void testZero() {
        //execute
        instance.execute(cpu, 1, 0);
        
        //assert flags
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getCarry());
        assertEquals(true, cpu.getSRAM().getStatusRegisterObj().getZero());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getNegative());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getOverflow());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getSign());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getHalfCarry());
    }  

    /**
     * Test of execute method, of class BSET.
     */
    @Test
    public void testNegative() {
        //execute
        instance.execute(cpu, 2, 0);
        
        //assert flags
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getCarry());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getZero());
        assertEquals(true, cpu.getSRAM().getStatusRegisterObj().getNegative());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getOverflow());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getSign());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getHalfCarry());
    }  

    /**
     * Test of execute method, of class BSET.
     */
    @Test
    public void testOverflow() {
        //execute
        instance.execute(cpu, 3, 0);
        
        //assert flags
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getCarry());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getZero());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getNegative());
        assertEquals(true, cpu.getSRAM().getStatusRegisterObj().getOverflow());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getSign());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getHalfCarry());
    }  

    /**
     * Test of execute method, of class BSET.
     */
    @Test
    public void testSign() {
        //execute
        instance.execute(cpu, 4, 0);
        
        //assert flags
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getCarry());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getZero());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getNegative());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getOverflow());
        assertEquals(true, cpu.getSRAM().getStatusRegisterObj().getSign());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getHalfCarry());
    }  

    /**
     * Test of execute method, of class BSET.
     */
    @Test
    public void testHalfCarry() {
        //execute
        instance.execute(cpu, 5, 0);
        
        //assert flags
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getCarry());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getZero());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getNegative());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getOverflow());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getSign());
        assertEquals(true, cpu.getSRAM().getStatusRegisterObj().getHalfCarry());
    }  

    /**
     * Test of execute method, of class BSET.
     */
    @Test
    public void testCopyStorage() {
        //execute
        instance.execute(cpu, 6, 0);
        
        assertEquals(0x40, cpu.getSRAM().getStatusRegister());
        
        //assert flags
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getCarry());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getZero());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getNegative());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getOverflow());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getSign());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getHalfCarry());
    }      

    /**
     * Test of execute method, of class BSET.
     */
    @Test
    public void testInterruptEnable() {
        //execute
        instance.execute(cpu, 7, 0);
        
        assertEquals(0x80, cpu.getSRAM().getStatusRegister());
        
        //assert flags
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getCarry());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getZero());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getNegative());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getOverflow());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getSign());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getHalfCarry());
    }      
    
}
