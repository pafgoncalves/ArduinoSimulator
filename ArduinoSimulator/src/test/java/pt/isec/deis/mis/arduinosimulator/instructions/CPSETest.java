/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.isec.deis.mis.arduinosimulator.instructions;

import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import pt.isec.deis.mis.arduinosimulator.ATmega328P;
import pt.isec.deis.mis.arduinosimulator.CPU;

/**
 *
 * @author pafgoncalves@ipc.pt
 */
public class CPSETest {
    
    static CPU cpu = null;
    static CPSE instance = null;
    
    public CPSETest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        cpu = new ATmega328P();
        instance = new CPSE();
        //place a NOP at address 1
        cpu.getFLASH().set(1, 0);
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
     * Test of execute method, of class CPSE.
     */
    @Test
    public void testEqual() throws Exception{
        int rdAddr = 0;
        int rrAddr = 1;
        cpu.getSRAM().setRegister(rdAddr, 1);
        cpu.getSRAM().setRegister(rrAddr, 1);

        int pc = cpu.getPc()+1;
        
        //execute
        instance.execute(cpu, rdAddr, rrAddr);
        
        //assert result
        assertEquals(pc+1, cpu.getPc());
    }

    /**
     * Test of execute method, of class CPSE.
     */
    @Test
    public void testNotEqual() throws Exception{
        int rdAddr = 0;
        int rrAddr = 1;
        cpu.getSRAM().setRegister(rdAddr, 1);
        cpu.getSRAM().setRegister(rrAddr, 2);

        int pc = cpu.getPc()+1;
        
        //execute
        instance.execute(cpu, rdAddr, rrAddr);
        
        //assert result
        assertEquals(pc, cpu.getPc());
    }

    
}
