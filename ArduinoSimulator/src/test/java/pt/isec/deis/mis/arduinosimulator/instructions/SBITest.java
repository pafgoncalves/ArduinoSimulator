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
public class SBITest {
    
    static CPU cpu = null;
    static SBI instance = null;
    
    public SBITest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        cpu = new ATmega328P();
        instance = new SBI();
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
     * Test of execute method, of class SBI.
     */
    @Test
    public void testExecute() {
        int A = 0x1E;
        int b = 2;
        cpu.getFLASH().set(100, 0);
        cpu.getSRAM().setIO(A, 0);
        
        //execute
        instance.execute(cpu, A, b);
        
        //assert result
        assertEquals(4, cpu.getSRAM().getIO(A));
    }

    
}
