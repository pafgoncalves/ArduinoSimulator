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
public class LDITest {
    
    static CPU cpu = null;
    static LDI instance = null;
    
    public LDITest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        cpu = new ATmega328P();
        instance = new LDI();
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
     * Test of execute method, of class LDI.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testExecute() {
        int rdAddr = 0;

        //execute
        instance.execute(cpu, rdAddr, 0xA5);
        
        //assert result
        assertEquals(0xA5, cpu.getSRAM().getRegister(rdAddr));
    }
    
    /**
     * Test of execute method, of class LDI.
     */
    @Test
    public void testExecute2() {
        int rdAddr = 16;

        //execute
        instance.execute(cpu, rdAddr, 0xA5);
        
        //assert result
        assertEquals(0xA5, cpu.getSRAM().getRegister(rdAddr));
    }
    
    /**
     * Test of execute method, of class LDI.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testExecute3() {
        int rdAddr = 32;

        //execute
        instance.execute(cpu, rdAddr, 0xA5);
        
        //assert result
        assertEquals(0xA5, cpu.getSRAM().getRegister(rdAddr));
    }
    
}
