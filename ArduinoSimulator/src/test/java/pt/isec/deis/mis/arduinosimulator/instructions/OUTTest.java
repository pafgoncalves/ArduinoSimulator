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
public class OUTTest {
    
    static CPU cpu = null;
    static OUT instance = null;

    public OUTTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        cpu = new ATmega328P();
        instance = new OUT();
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
     * Test of execute method, of class OUT.
     */
    @Test
    public void testExecute() {
        int rdAddr = 0;
        int value = 0xAA;
        int addr = 0x55;
        cpu.getSRAM().setRegister(rdAddr, value);
        
        //execute
        instance.execute(cpu, rdAddr, addr);
        
        //assert 
        assertEquals(value, cpu.getSRAM().get(addr));
    }
    
}
