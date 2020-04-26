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
public class LASTest {
    
    static CPU cpu = null;
    static LAS instance = null;
    
    public LASTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        cpu = new ATmega328P();
        instance = new LAS();
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
     * Test of execute method, of class LAS.
     */
    @Test
    public void testExecute() {
        int addr = 0x10;
        int rdAddr = 0;
        cpu.getSRAM().set(addr, 3);
        cpu.getSRAM().setRegisterZ(addr);
        cpu.getSRAM().setRegister(rdAddr, 0xF1);
        
        //execute
        instance.execute(cpu, rdAddr, 0);
        
        //assert result
        assertEquals(3, cpu.getSRAM().getRegister(rdAddr));
        assertEquals(0xF3, cpu.getSRAM().get(addr));
    }

    
}
