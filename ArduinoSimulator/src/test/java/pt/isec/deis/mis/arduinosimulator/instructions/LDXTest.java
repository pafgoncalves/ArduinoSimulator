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
public class LDXTest {
    
    static CPU cpu = null;
    static LDX instance = null;
    
    public LDXTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        cpu = new ATmega328P();
        instance = new LDX();
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
     * Test of execute method, of class LDX.
     */
    @Test
    public void testLDX() {
        int rdAddr = 0;
        int addr = 0x10;
        cpu.getSRAM().setRegister(rdAddr, 1);
        cpu.getSRAM().setRegisterX(addr);
        cpu.getSRAM().set(addr,0x20);
        
        //execute
        instance.execute(cpu, rdAddr, 0);
        
        //assert result
        assertEquals(0x20, cpu.getSRAM().getRegister(rdAddr));        
        assertEquals(addr, cpu.getSRAM().getRegisterX());        
    }

    @Test
    public void testLDX1() {
        int rdAddr = 0;
        int addr = 0x10;
        cpu.getSRAM().setRegister(rdAddr, 1);
        cpu.getSRAM().setRegisterX(addr);
        cpu.getSRAM().set(addr,0x20);
        
        //execute
        instance.execute(cpu, rdAddr, 1);
        
        //assert result
        assertEquals(0x20, cpu.getSRAM().getRegister(rdAddr));        
        assertEquals(addr+1, cpu.getSRAM().getRegisterX());        
    }

    @Test
    public void testLDX2() {
        int rdAddr = 0;
        int addr = 0x10;
        cpu.getSRAM().setRegister(rdAddr, 1);
        cpu.getSRAM().setRegisterX(addr);
        cpu.getSRAM().set(addr-1,0xA5);
        cpu.getSRAM().set(addr,0x20);
        
        //execute
        instance.execute(cpu, rdAddr, 2);
        
        //assert result
        assertEquals(0xA5, cpu.getSRAM().getRegister(rdAddr));        
        assertEquals(addr-1, cpu.getSRAM().getRegisterX());        
    }

    
}
