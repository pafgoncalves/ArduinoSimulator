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
public class STYTest {
    
    static CPU cpu = null;
    static STY instance = null;
    
    public STYTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        cpu = new ATmega328P();
        instance = new STY();
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
     * Test of execute method, of class STY.
     */
    @Test
    public void testSTY() {
        int rdAddr = 0;
        int addr = 0x100;
        cpu.getSRAM().setRegister(rdAddr, 0xA5);
        cpu.getSRAM().setRegisterY(addr);
        
        //execute
        instance.execute(cpu, rdAddr, 0);
        
        //assert result
        assertEquals(0xA5, cpu.getSRAM().get(addr));        
        assertEquals(addr, cpu.getSRAM().getRegisterY());        
    }

    @Test
    public void testSTY1() {
        int rdAddr = 0;
        int addr = 0x100;
        cpu.getSRAM().setRegister(rdAddr, 0xA5);
        cpu.getSRAM().setRegisterY(addr);
        
        //execute
        instance.execute(cpu, rdAddr, 65);
        
        //assert result
        assertEquals(0xA5, cpu.getSRAM().get(addr));        
        assertEquals(addr+1, cpu.getSRAM().getRegisterY());        
    }

    @Test
    public void testSTY2() {
        int rdAddr = 0;
        int addr = 0x100;
        cpu.getSRAM().setRegister(rdAddr, 0xA5);
        cpu.getSRAM().setRegisterY(addr);
        
        //execute
        instance.execute(cpu, rdAddr, 66);
        
        //assert result
        assertEquals(0xA5, cpu.getSRAM().get(addr-1));        
        assertEquals(addr-1, cpu.getSRAM().getRegisterY());        
    }
    
    @Test
    public void testSTY3() {
        int rdAddr = 0;
        int addr = 0x100;
        int q = 3;
        cpu.getSRAM().setRegister(rdAddr, 0xA5);
        cpu.getSRAM().setRegisterY(addr);
        
        //execute
        instance.execute(cpu, rdAddr, q);
        
        //assert result
        assertEquals(0xA5, cpu.getSRAM().get(addr+q));        
        assertEquals(addr, cpu.getSRAM().getRegisterY());        
    }
    
}
