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
public class INTest {
    
    static CPU cpu = null;
    static IN instance = null;

    public INTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        cpu = new ATmega328P();
        instance = new IN();
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
     * Test of execute method, of class IN.
     */
    @Test
    public void testExecute() {
        int rdAddr = 0;
        int value = 0xAA;
        int addr = 0x55;
        cpu.getSRAM().set(addr, value);
        
        //execute
        instance.execute(cpu, rdAddr, addr);
        
        //assert PC
        assertEquals(value, cpu.getSRAM().getRegister(rdAddr));
    }
    
}
