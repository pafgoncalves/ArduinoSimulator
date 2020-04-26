/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.isec.deis.mis.arduinosimulator.instructions;

import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import pt.isec.deis.mis.arduinosimulator.ATmega328P;
import pt.isec.deis.mis.arduinosimulator.CPU;

/**
 *
 * @author pafgoncalves@ipc.pt
 */
public class RCALLTest {
    
    static CPU cpu = null;
    static RCALL instance = null;
    
    public RCALLTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        cpu = new ATmega328P();
        instance = new RCALL();
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        cpu.reset();
        cpu.getSRAM().setStackPointer(0x8FF);
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of execute method, of class RCALL.
     */
    @Test
    public void testRCALL() {
        //execute
        instance.execute(cpu, 0x10, 0);
        
        //assert PC
        assertEquals(0x10+1, cpu.getPc());
        
        //assert SP
        assertEquals(0x8FF-2, cpu.getSRAM().getStackPointer());
        
        //assert stack
        assertEquals(0x01, cpu.getSRAM().get(0x8FF));
        assertEquals(0x00, cpu.getSRAM().get(0x8FF-1));
    }

    @Test
    public void testRCALL2() {
        cpu.setPc(20);
        
        //execute
        instance.execute(cpu, 0xFFF6, 0); //FFF6 => -10
        
        //assert PC
        assertEquals(20-10+1, cpu.getPc());
        
        //assert SP
        assertEquals(0x8FF-2, cpu.getSRAM().getStackPointer());
        
        //assert stack
        assertEquals(20+1, cpu.getSRAM().get(0x8FF));
        assertEquals(0x00, cpu.getSRAM().get(0x8FF-1));
    }

}
