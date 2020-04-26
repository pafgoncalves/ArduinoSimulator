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
public class RETITest {
    
    static CPU cpu = null;
    static RETI instance = null;
    
    public RETITest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        cpu = new ATmega328P();
        instance = new RETI();
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
     * Test of execute method, of class RETI.
     */
    @Test
    public void testExecute() {
        cpu.getSRAM().setStackPointer(0x8FF-2);
        cpu.getSRAM().set(0x8FF, 0x10);
        cpu.getSRAM().set(0x8FF-1, 0x00);
        
        //execute
        instance.execute(cpu, 0, 0);
        
        //assert PC
        assertEquals(0x10, cpu.getPc());
        
        //assert SP
        assertEquals(0x8FF, cpu.getSRAM().getStackPointer());
        
        //assert flags
        assertEquals(true, cpu.getSRAM().getStatusRegisterObj().getGlobalInterruptEnable());        
    }

    
}
