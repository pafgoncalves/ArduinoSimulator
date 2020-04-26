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
public class PUSHTest {
    
    static CPU cpu = null;
    static PUSH instance = null;
    
    public PUSHTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        cpu = new ATmega328P();
        instance = new PUSH();
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
     * Test of execute method, of class PUSH.
     */
    @Test
    public void testExecute() {
        int rrAddr = 1;
        cpu.getSRAM().setRegister(rrAddr, 0xA5);
        
        //execute
        instance.execute(cpu, rrAddr, 0);
        
        //assert result
        assertEquals(0xA5, cpu.getSRAM().get(0x8FF));
        
        //assert SP
        assertEquals(0x8FF-1, cpu.getSRAM().getStackPointer());
    }


    
}
