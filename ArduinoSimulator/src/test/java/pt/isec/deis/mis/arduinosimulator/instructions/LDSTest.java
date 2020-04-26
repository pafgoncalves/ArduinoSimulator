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
public class LDSTest {
    
    static CPU cpu = null;
    static LDS instance = null;
    
    public LDSTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        cpu = new ATmega328P();
        instance = new LDS();
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
     * Test of execute method, of class LDS.
     */
    @Test
    public void testExecute() {
        int addr = 0x100;
        int K = 0xA5;
        int rdAddr = 0;
        cpu.getSRAM().set(addr, K);
        
        //execute
        instance.execute(cpu, rdAddr, addr);
        
        //assert result
        assertEquals(K, cpu.getSRAM().getRegister(rdAddr));
    }
    
}
