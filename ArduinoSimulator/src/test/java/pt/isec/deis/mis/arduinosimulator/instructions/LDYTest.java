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
public class LDYTest {
    
    static CPU cpu = null;
    static LDY instance = null;
    
    public LDYTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        cpu = new ATmega328P();
        instance = new LDY();
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
     * Test of execute method, of class LDY.
     */
    @Test
    public void testLDY() {
        int rdAddr = 0;
        int addr = 0x10;
        cpu.getSRAM().setRegister(rdAddr, 1);
        cpu.getSRAM().setRegisterY(addr);
        cpu.getSRAM().set(addr,0x20);
        
        //execute
        instance.execute(cpu, rdAddr, 0);
        
        //assert result
        assertEquals(0x20, cpu.getSRAM().getRegister(rdAddr));        
        assertEquals(0x10, cpu.getSRAM().getRegisterY());        
    }

    @Test
    public void testLDY1() {
        int rdAddr = 0;
        int addr = 0x10;
        cpu.getSRAM().setRegister(rdAddr, 1);
        cpu.getSRAM().setRegisterY(addr);
        cpu.getSRAM().set(addr,0x20);
        
        //execute
        instance.execute(cpu, rdAddr, 65);
        
        //assert result
        assertEquals(0x20, cpu.getSRAM().getRegister(rdAddr));        
        assertEquals(0x10+1, cpu.getSRAM().getRegisterY());        
    }

    @Test
    public void testLDY2() {
        int rdAddr = 0;
        int addr = 0x10;
        cpu.getSRAM().setRegister(rdAddr, 1);
        cpu.getSRAM().setRegisterY(addr);
        cpu.getSRAM().set(addr-1,0xA5);
        cpu.getSRAM().set(addr,0x20);
        
        //execute
        instance.execute(cpu, rdAddr, 66);
        
        //assert result
        assertEquals(0xA5, cpu.getSRAM().getRegister(rdAddr));        
        assertEquals(0x10-1, cpu.getSRAM().getRegisterY());        
    }

    @Test
    public void testLDY3() {
        int rdAddr = 0;
        int addr = 0x10;
        cpu.getSRAM().setRegister(rdAddr, 1);
        cpu.getSRAM().setRegisterY(addr);
        cpu.getSRAM().set(addr,0x20);
        int q = 3;
        cpu.getSRAM().set(addr+q,0xA5);
        
        //execute
        instance.execute(cpu, rdAddr, q);
        
        //assert result
        assertEquals(0xA5, cpu.getSRAM().getRegister(rdAddr));        
        assertEquals(0x10, cpu.getSRAM().getRegisterY());        
    }
}
