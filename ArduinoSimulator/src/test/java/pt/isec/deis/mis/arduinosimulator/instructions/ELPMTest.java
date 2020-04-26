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
import pt.isec.deis.mis.arduinosimulator.NotSupportedInstructionException;

/**
 *
 * @author pafgoncalves@ipc.pt
 */
public class ELPMTest {
    
    static CPU cpu = null;
    static ELPM instance = null;
    
    public ELPMTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        cpu = new ATmega328P();
        instance = new ELPM();
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
     * Test of execute method, of class LPM.
     */
    @Test(expected = NotSupportedInstructionException.class)
    public void testELPM() throws Exception {
        cpu.getFLASH().set(0, 0x5AA5);
        cpu.getSRAM().setRegisterZ(0);
        
        //execute
        instance.execute(cpu, 0, 0);
        
        //validar o R0
        assertEquals(0xA5, cpu.getSRAM().getRegister(0));
        //validar se o Z ficou igual
        assertEquals(0, cpu.getSRAM().getRegisterZ());
    }

    
    @Test(expected = NotSupportedInstructionException.class)
    public void testELPM2() throws Exception {
        cpu.getFLASH().set(0, 0x5AA5);
        cpu.getSRAM().setRegisterZ(1);
        
        //execute
        instance.execute(cpu, 0, 0);
        
        //validar o R0
        assertEquals(0x5A, cpu.getSRAM().getRegister(0));
        //validar se o Z ficou igual
        assertEquals(1, cpu.getSRAM().getRegisterZ());
    }

    
    @Test(expected = NotSupportedInstructionException.class)
    public void testELPM3() throws Exception {
        int rdAddr = 1;
        cpu.getFLASH().set(0, 0x5AA5);
        cpu.getSRAM().setRegisterZ(rdAddr);
        
        //execute
        instance.execute(cpu, rdAddr, 0);
        
        //validar o R0
        assertEquals(0x5A, cpu.getSRAM().getRegister(rdAddr));
        //validar se o Z ficou igual
        assertEquals(rdAddr, cpu.getSRAM().getRegisterZ());
    }

    
    @Test(expected = NotSupportedInstructionException.class)
    public void testELPM4() throws Exception {
        int rdAddr = 1;
        cpu.getFLASH().set(0, 0x5AA5);
        cpu.getSRAM().setRegisterZ(rdAddr);
        
        //execute
        instance.execute(cpu, rdAddr, 1);
        
        //validar o R0
        assertEquals(0x5A, cpu.getSRAM().getRegister(rdAddr));
        //validar se o Z ficou igual
        assertEquals(rdAddr+1, cpu.getSRAM().getRegisterZ());
    }

    
}
