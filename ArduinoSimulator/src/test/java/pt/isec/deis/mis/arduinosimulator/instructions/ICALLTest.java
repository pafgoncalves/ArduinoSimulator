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
public class ICALLTest {
    
    static CPU cpu = null;
    static ICALL instance = null;

    public ICALLTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        cpu = new ATmega328P();
        instance = new ICALL();
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
     * Test of execute method, of class ICALL.
     */
    @Test
    public void testExecute() {
        cpu.getSRAM().setRegisterZ(0x0010);
        
        //execute
        instance.execute(cpu, 0, 0);
        
        //assert PC
        assertEquals(0x10, cpu.getPc());
        
        //assert SP
        assertEquals(0x8FF-2, cpu.getSRAM().getStackPointer());
        
        //assert stack
        assertEquals(0x01, cpu.getSRAM().get(0x8FF));
        assertEquals(0x00, cpu.getSRAM().get(0x8FF-1));
    }

    /**
     * Test of execute method, of class CALL.
     */
    @Test
    public void testExecute2() {
        cpu.setPc(0x100);
        cpu.getSRAM().setRegisterZ(0x0200);
        
        //execute
        instance.execute(cpu, 0, 0);
        
        //assert PC
        assertEquals(0x200, cpu.getPc());
        
        //assert SP
        assertEquals(0x8FF-2, cpu.getSRAM().getStackPointer());
        
        //assert stack
        assertEquals(0x01, cpu.getSRAM().get(0x8FF));
        assertEquals(0x01, cpu.getSRAM().get(0x8FF-1));
    }    
}
