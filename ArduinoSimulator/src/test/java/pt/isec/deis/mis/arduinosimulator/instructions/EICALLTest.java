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
public class EICALLTest {
    
    static CPU cpu = null;
    static EICALL instance = null;
    
    public EICALLTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        cpu = new ATmega328P();
        instance = new EICALL();
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
     * Test of execute method, of class EICALL.
     */
    @Test(expected = NotSupportedInstructionException.class) 
    public void testEICALL() throws Exception {
        cpu.getSRAM().setRegisterEIND(0);
        cpu.getSRAM().setRegisterZ(0x10);
        
        //execute
        instance.execute(cpu, 0, 0);
        
        //assert PC
        assertEquals(0x10, cpu.getPc());
        
        //assert SP
        assertEquals(0x8FF-2, cpu.getSRAM().getStackPointer());
        
        //assert stack
        assertEquals(0x02, cpu.getSRAM().get(0x8FF));
        assertEquals(0x00, cpu.getSRAM().get(0x8FF-1));
    }

    
}
