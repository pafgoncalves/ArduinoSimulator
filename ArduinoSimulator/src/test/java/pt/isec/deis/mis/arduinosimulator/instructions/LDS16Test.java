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
public class LDS16Test {
    
    static CPU cpu = null;
    static LDS16 instance = null;
    
    public LDS16Test() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        cpu = new ATmega328P();
        instance = new LDS16();
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
     * Test of execute method, of class LDS16.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testExecute() {
        int addr = 0x40;
        int K = 0xA5;
        int rdAddr = 0;
        cpu.getSRAM().set(addr, K);
        
        //execute
        instance.execute(cpu, rdAddr, addr);
        
        //assert result
        assertEquals(K, cpu.getSRAM().getRegister(rdAddr));
    }

    /**
     * Test of execute method, of class LDS16.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testExecute2() {
        int addr = 0x00;
        int K = 0xA5;
        int rdAddr = 16;
        cpu.getSRAM().set(addr, K);
        
        //execute
        instance.execute(cpu, rdAddr, addr);
        
        //assert result
        assertEquals(K, cpu.getSRAM().getRegister(rdAddr));
    }

    /**
     * Test of execute method, of class LDS16.
     */
    @Test
    public void testExecute3() {
        int addr = 0xBF;
        int K = 0xA5;
        int rdAddr = 16;
        cpu.getSRAM().set(addr, K);
        
        //execute
        instance.execute(cpu, rdAddr, addr);
        
        //assert result
        assertEquals(K, cpu.getSRAM().getRegister(rdAddr));
    }
    
}
