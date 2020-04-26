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
public class RJMPTest {
    
    static CPU cpu = null;
    static RJMP instance = null;
    
    public RJMPTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        cpu = new ATmega328P();
        instance = new RJMP();
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
     * Test of execute method, of class RJMP.
     */
    @Test
    public void testRJMP() {
        //execute
        instance.execute(cpu, 0x10, 0);
        
        //assert PC
        assertEquals(0x10+1, cpu.getPc());
    }

    @Test
    public void testRJMP2() {
        cpu.setPc(20);
        
        //execute
        instance.execute(cpu, 0xFFF6, 0); //FFF6 => -10
        
        //assert PC
        assertEquals(20-10+1, cpu.getPc());
    }

}
