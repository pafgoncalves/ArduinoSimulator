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
public class JMPTest {
    
    static CPU cpu = null;
    static JMP instance = null;
    
    public JMPTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        cpu = new ATmega328P();
        instance = new JMP();
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
     * Test of execute method, of class JMP.
     */
    @Test
    public void testExecute() {
        //execute
        instance.execute(cpu, 0x10, 0);
        
        //assert PC
        assertEquals(0x10, cpu.getPc());
    }
    
    /**
     * Test of execute method, of class JMP.
     */
    @Test
    public void testExecute2() {
        //execute
        instance.execute(cpu, 0x200, 0);
        
        //assert PC
        assertEquals(0x200, cpu.getPc());
    }
    
    /**
     * Test of execute method, of class JMP.
     */
    @Test
    public void testExecuteWrapAround() {
        //execute
        instance.execute(cpu, 64*1024, 0);
        
        //assert PC
        assertEquals(0x0, cpu.getPc());
    }
    
    /**
     * Test of execute method, of class JMP.
     */
    @Test
    public void testExecuteWrapAround2() {
        //execute
        instance.execute(cpu, 65*1024, 0);
        
        //assert PC
        assertEquals(1024, cpu.getPc());
    }
    
}
