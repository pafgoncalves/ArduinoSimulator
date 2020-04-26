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
public class BRBCTest {
    
    static CPU cpu = null;
    static BRBC instance = null;

    public BRBCTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        cpu = new ATmega328P();
        instance = new BRBC();
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
     * Test of execute method, of class BRBC.
     */
    @Test
    public void testBRBCCarryCleared() {
        cpu.getSRAM().getStatusRegisterObj().setCarry(false);
        int pc = cpu.getPc()+1;
        
        //execute
        instance.execute(cpu, 0, 10);
        
        //assert result
        assertEquals(pc+10, cpu.getPc());
    }  

    /**
     * Test of execute method, of class BRBC.
     */
    @Test
    public void testBRBCCarrySet() {
        cpu.getSRAM().getStatusRegisterObj().setCarry(true);
        int pc = cpu.getPc()+1;
        
        //execute
        instance.execute(cpu, 0, 10);
        
        //assert result
        assertEquals(pc, cpu.getPc());
    }  

    /**
     * Test of execute method, of class BRBC.
     */
    @Test
    public void testBRBCZeroCleared() {
        cpu.getSRAM().getStatusRegisterObj().setZero(false);
        int pc = cpu.getPc()+1;
        
        //execute
        instance.execute(cpu, 1, 10);
        
        //assert result
        assertEquals(pc+10, cpu.getPc());
    }  

    /**
     * Test of execute method, of class BRBC.
     */
    @Test
    public void testBRBCZeroSet() {
        cpu.getSRAM().getStatusRegisterObj().setZero(true);
        int pc = cpu.getPc()+1;
        
        //execute
        instance.execute(cpu, 1, 10);
        
        //assert result
        assertEquals(pc, cpu.getPc());
    }  

    /**
     * Test of execute method, of class BRBC.
     */
    @Test
    public void testBRBCNegativeCleared() {
        cpu.getSRAM().getStatusRegisterObj().setNegative(false);
        int pc = cpu.getPc()+1;
        
        //execute
        instance.execute(cpu, 2, 10);
        
        //assert result
        assertEquals(pc+10, cpu.getPc());
    }  

    /**
     * Test of execute method, of class BRBC.
     */
    @Test
    public void testBRBCNegativeSet() {
        cpu.getSRAM().getStatusRegisterObj().setNegative(true);
        int pc = cpu.getPc()+1;
        
        //execute
        instance.execute(cpu, 2, 10);
        
        //assert result
        assertEquals(pc, cpu.getPc());
    }  

    /**
     * Test of execute method, of class BRBC.
     */
    @Test
    public void testBRBCOverflowCleared() {
        cpu.getSRAM().getStatusRegisterObj().setOverflow(false);
        int pc = cpu.getPc()+1;
        
        //execute
        instance.execute(cpu, 3, 10);
        
        //assert result
        assertEquals(pc+10, cpu.getPc());
    }  

    /**
     * Test of execute method, of class BRBC.
     */
    @Test
    public void testBRBCOverflowSet() {
        cpu.getSRAM().getStatusRegisterObj().setOverflow(true);
        int pc = cpu.getPc()+1;
        
        //execute
        instance.execute(cpu, 3, 10);
        
        //assert result
        assertEquals(pc, cpu.getPc());
    }  

    /**
     * Test of execute method, of class BRBC.
     */
    @Test
    public void testBRBCSignCleared() {
        cpu.getSRAM().getStatusRegisterObj().setSign(false);
        int pc = cpu.getPc()+1;
        
        //execute
        instance.execute(cpu, 4, 10);
        
        //assert result
        assertEquals(pc+10, cpu.getPc());
    }  

    /**
     * Test of execute method, of class BRBC.
     */
    @Test
    public void testBRBCSignSet() {
        cpu.getSRAM().getStatusRegisterObj().setSign(true);
        int pc = cpu.getPc()+1;
        
        //execute
        instance.execute(cpu, 4, 10);
        
        //assert result
        assertEquals(pc, cpu.getPc());
    }  

    /**
     * Test of execute method, of class BRBC.
     */
    @Test
    public void testBRBCHalfCarryCleared() {
        cpu.getSRAM().getStatusRegisterObj().setHalfCarry(false);
        int pc = cpu.getPc()+1;
        
        //execute
        instance.execute(cpu, 5, 10);
        
        //assert result
        assertEquals(pc+10, cpu.getPc());
    }  

    /**
     * Test of execute method, of class BRBC.
     */
    @Test
    public void testBRBCHalfCarrySet() {
        cpu.getSRAM().getStatusRegisterObj().setHalfCarry(true);
        int pc = cpu.getPc()+1;
        
        //execute
        instance.execute(cpu, 5, 10);
        
        //assert result
        assertEquals(pc, cpu.getPc());
    }  

    /**
     * Test of execute method, of class BRBC.
     */
    @Test
    public void testBRBCCopyStorageCleared() {
        cpu.getSRAM().getStatusRegisterObj().setCopyStorage(false);
        int pc = cpu.getPc()+1;
        
        //execute
        instance.execute(cpu, 6, 10);
        
        //assert result
        assertEquals(pc+10, cpu.getPc());
    }  

    /**
     * Test of execute method, of class BRBC.
     */
    @Test
    public void testBRBCCopyStorageSet() {
        cpu.getSRAM().getStatusRegisterObj().setCopyStorage(true);
        int pc = cpu.getPc()+1;
        
        //execute
        instance.execute(cpu, 6, 10);
        
        //assert result
        assertEquals(pc, cpu.getPc());
    }  

    /**
     * Test of execute method, of class BRBC.
     */
    @Test
    public void testBRBCGlobalInterruptEnableCleared() {
        cpu.getSRAM().getStatusRegisterObj().setGlobalInterruptEnable(false);
        int pc = cpu.getPc()+1;
        
        //execute
        instance.execute(cpu, 7, 10);
        
        //assert result
        assertEquals(pc+10, cpu.getPc());
    }  

    /**
     * Test of execute method, of class BRBC.
     */
    @Test
    public void testBRBCGlobalInterruptEnableSet() {
        cpu.getSRAM().getStatusRegisterObj().setGlobalInterruptEnable(true);
        int pc = cpu.getPc()+1;
        
        //execute
        instance.execute(cpu, 7, 10);
        
        //assert result
        assertEquals(pc, cpu.getPc());
    }  

}
