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
public class MOVWTest {
    
    static CPU cpu = null;
    static MOVW instance = null;
    
    public MOVWTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        cpu = new ATmega328P();
        instance = new MOVW();
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
     * Test of execute method, of class MOVW.
     */
    @Test
    public void testExecute() {
        int rdAddr = 0;
        int rrAddr = 2;
        cpu.getSRAM().setRegister(rdAddr, 0);
        cpu.getSRAM().setRegister(rdAddr+1, 0);
        cpu.getSRAM().setRegister(rrAddr, 0xA5);
        cpu.getSRAM().setRegister(rrAddr+1, 0x5A);
        
        //execute
        instance.execute(cpu, rdAddr, rrAddr);
        
        //assert result
        assertEquals(0xA5, cpu.getSRAM().getRegister(rdAddr));
        assertEquals(0x5A, cpu.getSRAM().getRegister(rdAddr+1));
    }

    
}
