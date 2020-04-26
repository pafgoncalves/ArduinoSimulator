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
public class BRBSTest {
    
    static CPU cpu = null;
    static BRBS instance = null;

    public BRBSTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        cpu = new ATmega328P();
        instance = new BRBS();
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
     * Test of execute method, of class BRBS.
     */
    @Test
    public void testBRBSCarryCleared() {
        cpu.getSRAM().getStatusRegisterObj().setCarry(false);
        int pc = cpu.getPc()+1;
        
        //execute
        instance.execute(cpu, 0, 10);
        
        //assert result
        assertEquals(pc, cpu.getPc());
    }  

    /**
     * Test of execute method, of class BRBS.
     */
    @Test
    public void testBRBSCarrySet() {
        cpu.getSRAM().getStatusRegisterObj().setCarry(true);
        int pc = cpu.getPc()+1;
        
        //execute
        instance.execute(cpu, 0, 10);
        
        //assert result
        assertEquals(pc+10, cpu.getPc());
    }  

    /**
     * Test of execute method, of class BRBS.
     */
    @Test
    public void testBRBSZeroCleared() {
        cpu.getSRAM().getStatusRegisterObj().setZero(false);
        int pc = cpu.getPc()+1;
        
        //execute
        instance.execute(cpu, 1, 10);
        
        //assert result
        assertEquals(pc, cpu.getPc());
    }  

    /**
     * Test of execute method, of class BRBS.
     */
    @Test
    public void testBRBSZeroSet() {
        cpu.getSRAM().getStatusRegisterObj().setZero(true);
        int pc = cpu.getPc()+1;
        
        //execute
        instance.execute(cpu, 1, 10);
        
        //assert result
        assertEquals(pc+10, cpu.getPc());
    }  

    /**
     * Test of execute method, of class BRBS.
     */
    @Test
    public void testBRBSNegativeCleared() {
        cpu.getSRAM().getStatusRegisterObj().setNegative(false);
        int pc = cpu.getPc()+1;
        
        //execute
        instance.execute(cpu, 2, 10);
        
        //assert result
        assertEquals(pc, cpu.getPc());
    }  

    /**
     * Test of execute method, of class BRBS.
     */
    @Test
    public void testBRBSNegativeSet() {
        cpu.getSRAM().getStatusRegisterObj().setNegative(true);
        int pc = cpu.getPc()+1;
        
        //execute
        instance.execute(cpu, 2, 10);
        
        //assert result
        assertEquals(pc+10, cpu.getPc());
    }  

    /**
     * Test of execute method, of class BRBS.
     */
    @Test
    public void testBRBSOverflowCleared() {
        cpu.getSRAM().getStatusRegisterObj().setOverflow(false);
        int pc = cpu.getPc()+1;
        
        //execute
        instance.execute(cpu, 3, 10);
        
        //assert result
        assertEquals(pc, cpu.getPc());
    }  

    /**
     * Test of execute method, of class BRBS.
     */
    @Test
    public void testBRBSOverflowSet() {
        cpu.getSRAM().getStatusRegisterObj().setOverflow(true);
        int pc = cpu.getPc()+1;
        
        //execute
        instance.execute(cpu, 3, 10);
        
        //assert result
        assertEquals(pc+10, cpu.getPc());
    }  

    /**
     * Test of execute method, of class BRBS.
     */
    @Test
    public void testBRBSSignCleared() {
        cpu.getSRAM().getStatusRegisterObj().setSign(false);
        int pc = cpu.getPc()+1;
        
        //execute
        instance.execute(cpu, 4, 10);
        
        //assert result
        assertEquals(pc, cpu.getPc());
    }  

    /**
     * Test of execute method, of class BRBS.
     */
    @Test
    public void testBRBSSignSet() {
        cpu.getSRAM().getStatusRegisterObj().setSign(true);
        int pc = cpu.getPc()+1;
        
        //execute
        instance.execute(cpu, 4, 10);
        
        //assert result
        assertEquals(pc+10, cpu.getPc());
    }  

    /**
     * Test of execute method, of class BRBS.
     */
    @Test
    public void testBRBSHalfCarryCleared() {
        cpu.getSRAM().getStatusRegisterObj().setHalfCarry(false);
        int pc = cpu.getPc()+1;
        
        //execute
        instance.execute(cpu, 5, 10);
        
        //assert result
        assertEquals(pc, cpu.getPc());
    }  

    /**
     * Test of execute method, of class BRBS.
     */
    @Test
    public void testBRBSHalfCarrySet() {
        cpu.getSRAM().getStatusRegisterObj().setHalfCarry(true);
        int pc = cpu.getPc()+1;
        
        //execute
        instance.execute(cpu, 5, 10);
        
        //assert result
        assertEquals(pc+10, cpu.getPc());
    }  

    /**
     * Test of execute method, of class BRBS.
     */
    @Test
    public void testBRBSCopyStorageCleared() {
        cpu.getSRAM().getStatusRegisterObj().setCopyStorage(false);
        int pc = cpu.getPc()+1;
        
        //execute
        instance.execute(cpu, 6, 10);
        
        //assert result
        assertEquals(pc, cpu.getPc());
    }  

    /**
     * Test of execute method, of class BRBS.
     */
    @Test
    public void testBRBSCopyStorageSet() {
        cpu.getSRAM().getStatusRegisterObj().setCopyStorage(true);
        int pc = cpu.getPc()+1;
        
        //execute
        instance.execute(cpu, 6, 10);
        
        //assert result
        assertEquals(pc+10, cpu.getPc());
    }  

    /**
     * Test of execute method, of class BRBS.
     */
    @Test
    public void testBRBSGlobalInterruptEnableCleared() {
        cpu.getSRAM().getStatusRegisterObj().setGlobalInterruptEnable(false);
        int pc = cpu.getPc()+1;
        
        //execute
        instance.execute(cpu, 7, 10);
        
        //assert result
        assertEquals(pc, cpu.getPc());
    }  

    /**
     * Test of execute method, of class BRBS.
     */
    @Test
    public void testBRBSGlobalInterruptEnableSet() {
        cpu.getSRAM().getStatusRegisterObj().setGlobalInterruptEnable(true);
        int pc = cpu.getPc()+1;
        
        //execute
        instance.execute(cpu, 7, 10);
        
        //assert result
        assertEquals(pc+10, cpu.getPc());
    }  
    
}
