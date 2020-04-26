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
public class LDZTest {
    
    static CPU cpu = null;
    static LDZ instance = null;
    
    public LDZTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        cpu = new ATmega328P();
        instance = new LDZ();
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
     * Test of execute method, of class LDZ.
     */
    @Test
    public void testLDZ() {
        int rdAddr = 0;
        int addr = 0x10;
        cpu.getSRAM().setRegister(rdAddr, 1);
        cpu.getSRAM().setRegisterZ(addr);
        cpu.getSRAM().set(addr,0x20);
        
        //execute
        instance.execute(cpu, rdAddr, 0);
        
        //assert result
        assertEquals(0x20, cpu.getSRAM().getRegister(rdAddr));        
        assertEquals(0x10, cpu.getSRAM().getRegisterZ());        
    }

    @Test
    public void testLDZ1() {
        int rdAddr = 0;
        int addr = 0x10;
        cpu.getSRAM().setRegister(rdAddr, 1);
        cpu.getSRAM().setRegisterZ(addr);
        cpu.getSRAM().set(addr,0x20);
        
        //execute
        instance.execute(cpu, rdAddr, 65);
        
        //assert result
        assertEquals(0x20, cpu.getSRAM().getRegister(rdAddr));        
        assertEquals(0x10+1, cpu.getSRAM().getRegisterZ());        
    }

    @Test
    public void testLDZ2() {
        int rdAddr = 0;
        int addr = 0x10;
        cpu.getSRAM().setRegister(rdAddr, 1);
        cpu.getSRAM().setRegisterZ(addr);
        cpu.getSRAM().set(addr-1,0xA5);
        cpu.getSRAM().set(addr,0x20);
        
        //execute
        instance.execute(cpu, rdAddr, 66);
        
        //assert result
        assertEquals(0xA5, cpu.getSRAM().getRegister(rdAddr));        
        assertEquals(0x10-1, cpu.getSRAM().getRegisterZ());        
    }

    @Test
    public void testLDZ3() {
        int rdAddr = 0;
        int addr = 0x10;
        cpu.getSRAM().setRegister(rdAddr, 1);
        cpu.getSRAM().setRegisterZ(addr);
        cpu.getSRAM().set(addr,0x20);
        int q = 3;
        cpu.getSRAM().set(addr+q,0xA5);
        
        //execute
        instance.execute(cpu, rdAddr, q);
        
        //assert result
        assertEquals(0xA5, cpu.getSRAM().getRegister(rdAddr));        
        assertEquals(0x10, cpu.getSRAM().getRegisterZ());        
    }
    
}
