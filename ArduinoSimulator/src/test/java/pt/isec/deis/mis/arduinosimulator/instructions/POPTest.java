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
public class POPTest {
    
    static CPU cpu = null;
    static POP instance = null;
    
    public POPTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        cpu = new ATmega328P();
        instance = new POP();
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        cpu.reset();
        cpu.getSRAM().setStackPointer(0x8FF-1);
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of execute method, of class POP.
     */
    @Test
    public void testExecute() {
        int rrAddr = 1;
        cpu.getSRAM().set(0x8FF, 0xA5);
        
        //execute
        instance.execute(cpu, rrAddr, 0);
        
        //assert result
        assertEquals(0xA5, cpu.getSRAM().getRegister(rrAddr));
        
        //assert SP
        assertEquals(0x8FF, cpu.getSRAM().getStackPointer());
    }


    
}
