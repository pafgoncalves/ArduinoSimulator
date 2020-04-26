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
public class SBRCTest {
    
    static CPU cpu = null;
    static SBRC instance = null;
    
    public SBRCTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        cpu = new ATmega328P();
        instance = new SBRC();
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
     * Test of execute method, of class SBRC.
     */
    @Test
    public void testSBRCCleared() throws Exception {
        int rrAddr = 0;
        int b = 1;
        cpu.getFLASH().set(100, 0);
        cpu.getSRAM().setRegister(rrAddr, 0);
        
        //execute
        instance.execute(cpu, rrAddr, b);
        
        //assert result
        assertEquals(2, cpu.getPc());
    }
    
    @Test
    public void testSBRCSet() throws Exception {
        int rrAddr = 0;
        int b = 1;
        cpu.getFLASH().set(100, 0);
        cpu.getSRAM().setRegister(rrAddr, 3);
        
        //execute
        instance.execute(cpu, rrAddr, b);
        
        //assert result
        assertEquals(1, cpu.getPc());
    }

    
}
