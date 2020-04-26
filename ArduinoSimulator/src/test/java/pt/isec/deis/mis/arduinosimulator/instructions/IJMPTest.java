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
public class IJMPTest {
    
    static CPU cpu = null;
    static IJMP instance = null;

    public IJMPTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        cpu = new ATmega328P();
        instance = new IJMP();
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
     * Test of execute method, of class IJMP.
     */
    @Test
    public void testExecute() {
        cpu.getSRAM().setRegisterZ(0x0010);
        
        //execute
        instance.execute(cpu, 0, 0);
        
        //assert PC
        assertEquals(0x10, cpu.getPc());
    }

    /**
     * Test of execute method, of class IJMP.
     */
    @Test
    public void testExecute2() {
        cpu.setPc(0x100);
        cpu.getSRAM().setRegisterZ(0x0200);
        
        //execute
        instance.execute(cpu, 0, 0);
        
        //assert PC
        assertEquals(0x200, cpu.getPc());
    }    
    
}
