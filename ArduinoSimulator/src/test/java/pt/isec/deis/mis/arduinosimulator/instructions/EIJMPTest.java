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
import pt.isec.deis.mis.arduinosimulator.NotSupportedInstructionException;

/**
 *
 * @author pafgoncalves@ipc.pt
 */
public class EIJMPTest {
    
    static CPU cpu = null;
    static EIJMP instance = null;
    
    public EIJMPTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        cpu = new ATmega328P();
        instance = new EIJMP();
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
     * Test of execute method, of class EIJMP.
     */
    @Test(expected = NotSupportedInstructionException.class)
    public void testExecute() throws Exception {
        cpu.getSRAM().setRegisterEIND(0x0000);
        cpu.getSRAM().setRegisterZ(0x0010);
        
        //execute
        instance.execute(cpu, 0, 0);
        
        //assert PC
        assertEquals(0x10, cpu.getPc());

    }

    
}
