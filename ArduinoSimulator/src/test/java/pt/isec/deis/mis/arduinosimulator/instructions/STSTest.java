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
import pt.isec.deis.mis.arduinosimulator.InstructionParams;
import static pt.isec.deis.mis.arduinosimulator.instructions.ADDTest.cpu;

/**
 *
 * @author pafgoncalves@ipc.pt
 */
public class STSTest {
    
    static CPU cpu = null;
    static STS instance = null;
    
    public STSTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        cpu = new ATmega328P();
        instance = new STS();
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
     * Test of execute method, of class STS.
     */
    @Test
    public void testExecute() {
        int rdAddr = 0;
        int K = 0x100;
        cpu.getSRAM().setRegister(rdAddr, 0xA5);
        
        //execute
        instance.execute(cpu, rdAddr, K);
        
        //assert result
        assertEquals(0xA5, cpu.getSRAM().get(K));
    }

}
