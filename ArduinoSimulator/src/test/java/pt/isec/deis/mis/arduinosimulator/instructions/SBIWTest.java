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
public class SBIWTest {
    
    static CPU cpu = null;
    static SBIW instance = null;
    
    public SBIWTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        cpu = new ATmega328P();
        instance = new SBIW();
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
     * Test of execute method, of class SBIW.
     */
    @Test
    public void testSBIW() {
        int rdAddr = 24;
        int K = 1;
        cpu.getSRAM().setRegister(rdAddr, 2);
        cpu.getSRAM().setRegister(rdAddr+1, 0);
        
        //execute
        instance.execute(cpu, rdAddr, K);
        
        //assert result
        assertEquals(1, cpu.getSRAM().getRegister(rdAddr));
        assertEquals(0, cpu.getSRAM().getRegister(rdAddr+1));
        
        //assert flags
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getCarry());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getZero());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getNegative());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getOverflow());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getSign());
    }

    @Test
    public void testSBIW2() {
        int rdAddr = 24;
        int K = 1;
        cpu.getSRAM().setRegister(rdAddr, 2);
        cpu.getSRAM().setRegister(rdAddr+1, 1);
        
        //execute
        instance.execute(cpu, rdAddr, K);
        
        //assert result
        assertEquals(1, cpu.getSRAM().getRegister(rdAddr));
        assertEquals(1, cpu.getSRAM().getRegister(rdAddr+1));
        
        //assert flags
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getCarry());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getZero());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getNegative());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getOverflow());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getSign());
    }

    @Test
    public void testSBIW3() {
        int rdAddr = 24;
        int K = 3;
        cpu.getSRAM().setRegister(rdAddr, 2);
        cpu.getSRAM().setRegister(rdAddr+1, 1);
        
        //execute
        instance.execute(cpu, rdAddr, K);
        
        //assert result
        assertEquals(0xFF, cpu.getSRAM().getRegister(rdAddr));
        assertEquals(0, cpu.getSRAM().getRegister(rdAddr+1));
        
        //assert flags
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getCarry());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getZero());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getNegative());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getOverflow());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getSign());
    }

    /**
     * Test of execute method, of class SBIW.
     */
    @Test
    public void testZero() {
        int rdAddr = 24;
        int K = 1;
        cpu.getSRAM().setRegister(rdAddr, 1);
        cpu.getSRAM().setRegister(rdAddr+1, 0);
        
        //execute
        instance.execute(cpu, rdAddr, K);
        
        //assert result
        assertEquals(0, cpu.getSRAM().getRegister(rdAddr));
        assertEquals(0, cpu.getSRAM().getRegister(rdAddr+1));
        
        //assert flags
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getCarry());
        assertEquals(true, cpu.getSRAM().getStatusRegisterObj().getZero());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getNegative());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getOverflow());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getSign());
    }

    /**
     * Test of execute method, of class SBIW.
     */
    @Test
    public void testOverflow() {
        int rdAddr = 24;
        int K = 1;
        cpu.getSRAM().setRegister(rdAddr, -128);
        cpu.getSRAM().setRegister(rdAddr+1, 0);
        
        //execute
        instance.execute(cpu, rdAddr, K);
        
        //assert result
        assertEquals(0x7F, cpu.getSRAM().getRegister(rdAddr));
        assertEquals(0, cpu.getSRAM().getRegister(rdAddr+1));
        
        //assert flags
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getCarry());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getZero());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getNegative());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getOverflow());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getSign());
    }

   

    /**
     * Test of execute method, of class SBIW.
     */
    @Test
    public void testCarry() {
        int rdAddr = 24;
        int K = 10;
        cpu.getSRAM().setRegister(rdAddr, 5);
        cpu.getSRAM().setRegister(rdAddr+1, 0);

        //execute
        instance.execute(cpu, rdAddr, K);
        
        //assert result
        assertEquals(0xFB, cpu.getSRAM().getRegister(rdAddr));
        assertEquals(0xFF, cpu.getSRAM().getRegister(rdAddr+1));
        
        //assert flags
        assertEquals(true, cpu.getSRAM().getStatusRegisterObj().getCarry());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getZero());
        assertEquals(true, cpu.getSRAM().getStatusRegisterObj().getNegative());
        assertEquals(true, cpu.getSRAM().getStatusRegisterObj().getOverflow());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getSign());
    }

    /**
     * Test of execute method, of class SBIW.
     */
    @Test
    public void testHalfCarry() {
        int rdAddr = 24;
        int K = 8;
        cpu.getSRAM().setRegister(rdAddr, 18);
        cpu.getSRAM().setRegister(rdAddr+1, 0);

        //execute
        instance.execute(cpu, rdAddr, K);
        
        //assert result
        assertEquals(10, cpu.getSRAM().getRegister(rdAddr));
        assertEquals(0, cpu.getSRAM().getRegister(rdAddr+1));
        
        //assert flags
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getCarry());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getZero());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getNegative());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getOverflow());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getSign());
    }
    
}
