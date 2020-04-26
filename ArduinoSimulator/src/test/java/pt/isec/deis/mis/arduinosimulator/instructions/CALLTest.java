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
public class CALLTest {
    
    static CPU cpu = null;
    static CALL instance = null;

    public CALLTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        cpu = new ATmega328P();
        instance = new CALL();
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
     * Test of execute method, of class CALL.
     */
    @Test
    public void testExecute() throws Exception {
        //execute
        instance.execute(cpu, 0x10, 0);
        
        //assert PC
        assertEquals(0x10, cpu.getPc());
        
        //assert SP
        assertEquals(0x8FF-2, cpu.getSRAM().getStackPointer());
        
        //assert stack
        assertEquals(0x02, cpu.getSRAM().get(0x8FF));
        assertEquals(0x00, cpu.getSRAM().get(0x8FF-1));
    }

    /**
     * Test of execute method, of class CALL.
     */
    @Test
    public void testExecute2() throws Exception {
        cpu.setPc(0x100);
        
        //execute
        instance.execute(cpu, 0x200, 0);
        
        //assert PC
        assertEquals(0x200, cpu.getPc());
        
        //assert SP
        assertEquals(0x8FF-2, cpu.getSRAM().getStackPointer());
        
        //assert stack
        assertEquals(0x02, cpu.getSRAM().get(0x8FF));
        assertEquals(0x01, cpu.getSRAM().get(0x8FF-1));
    }

    /**
     * Test of execute method, of class CALL.
     */
    @Test
    public void testExecuteWrapAround() throws Exception {
        cpu.setPc(0x100);
        
        //execute
        instance.execute(cpu, 64*1024, 0);
        
        //assert PC
        assertEquals(0x0, cpu.getPc());
        
        //assert SP
        assertEquals(0x8FF-2, cpu.getSRAM().getStackPointer());
        
        //assert stack
        assertEquals(0x02, cpu.getSRAM().get(0x8FF));
        assertEquals(0x01, cpu.getSRAM().get(0x8FF-1));
    }

    /**
     * Test of execute method, of class CALL.
     */
    @Test
    public void testExecuteWrapAround2() throws Exception {
        cpu.setPc(0x100);
        
        //execute
        instance.execute(cpu, 65*1024, 0);
        
        //assert PC
        assertEquals(1024, cpu.getPc());
        
        //assert SP
        assertEquals(0x8FF-2, cpu.getSRAM().getStackPointer());
        
        //assert stack
        assertEquals(0x02, cpu.getSRAM().get(0x8FF));
        assertEquals(0x01, cpu.getSRAM().get(0x8FF-1));
    }

    
}
