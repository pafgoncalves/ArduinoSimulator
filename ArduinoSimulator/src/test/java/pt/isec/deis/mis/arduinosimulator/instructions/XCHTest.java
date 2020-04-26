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
public class XCHTest {
    
    static CPU cpu = null;
    static XCH instance = null;
    
    public XCHTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        cpu = new ATmega328P();
        instance = new XCH();
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
     * Test of execute method, of class XCH.
     */
    @Test
    public void testExecute() {
        int rdAddr = 0;
        int addr = 0x100;
        cpu.getSRAM().setRegister(rdAddr, 0xA5);
        cpu.getSRAM().setRegisterZ(addr);
        cpu.getSRAM().set(addr, 0x4B);
        
        //execute
        instance.execute(cpu, rdAddr, 0);
        
        //assert result
        assertEquals(0x4B, cpu.getSRAM().getRegister(rdAddr));
        assertEquals(0xA5, cpu.getSRAM().get(addr));
    }

}
