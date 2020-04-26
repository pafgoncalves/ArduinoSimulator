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
public class BLDTest {
    
    static CPU cpu = null;
    static BLD instance = null;

    public BLDTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        cpu = new ATmega328P();
        instance = new BLD();
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
     * Test of execute method, of class BLD.
     */
    @Test
    public void testBLD00() {
        int rdAddr = 0;
        cpu.getSRAM().setRegister(rdAddr, 0);
        cpu.getSRAM().setStatusRegister(0x40);

        //execute
        instance.execute(cpu, rdAddr, 0);
        
        //assert result
        assertEquals(1, cpu.getSRAM().getRegister(rdAddr));
    }  

    /**
     * Test of execute method, of class BLD.
     */
    @Test
    public void testBLD01() {
        int rdAddr = 0;
        cpu.getSRAM().setRegister(rdAddr, 0xFF);
        cpu.getSRAM().setStatusRegister(0x00);

        //execute
        instance.execute(cpu, rdAddr, 0);
        
        //assert result
        assertEquals(0xFE, cpu.getSRAM().getRegister(rdAddr));
    }  

    /**
     * Test of execute method, of class BLD.
     */
    @Test
    public void testBLD10() {
        int rdAddr = 0;
        cpu.getSRAM().setRegister(rdAddr, 0);
        cpu.getSRAM().setStatusRegister(0x40);

        //execute
        instance.execute(cpu, rdAddr, 1);
        
        //assert result
        assertEquals(2, cpu.getSRAM().getRegister(rdAddr));
    }  

    /**
     * Test of execute method, of class BLD.
     */
    @Test
    public void testBLD11() {
        int rdAddr = 0;
        cpu.getSRAM().setRegister(rdAddr, 0xFF);
        cpu.getSRAM().setStatusRegister(0x00);

        //execute
        instance.execute(cpu, rdAddr, 1);
        
        //assert result
        assertEquals(0xFD, cpu.getSRAM().getRegister(rdAddr));
    }  

    /**
     * Test of execute method, of class BLD.
     */
    @Test
    public void testBLD20() {
        int rdAddr = 0;
        cpu.getSRAM().setRegister(rdAddr, 0);
        cpu.getSRAM().setStatusRegister(0x40);

        //execute
        instance.execute(cpu, rdAddr, 2);
        
        //assert result
        assertEquals(4, cpu.getSRAM().getRegister(rdAddr));
    }  

    /**
     * Test of execute method, of class BLD.
     */
    @Test
    public void testBLD21() {
        int rdAddr = 0;
        cpu.getSRAM().setRegister(rdAddr, 0xFF);
        cpu.getSRAM().setStatusRegister(0x00);

        //execute
        instance.execute(cpu, rdAddr, 2);
        
        //assert result
        assertEquals(0xFB, cpu.getSRAM().getRegister(rdAddr));
    }  

    /**
     * Test of execute method, of class BLD.
     */
    @Test
    public void testBLD30() {
        int rdAddr = 0;
        cpu.getSRAM().setRegister(rdAddr, 0);
        cpu.getSRAM().setStatusRegister(0x40);

        //execute
        instance.execute(cpu, rdAddr, 3);
        
        //assert result
        assertEquals(8, cpu.getSRAM().getRegister(rdAddr));
    }  

    /**
     * Test of execute method, of class BLD.
     */
    @Test
    public void testBLD31() {
        int rdAddr = 0;
        cpu.getSRAM().setRegister(rdAddr, 0xFF);
        cpu.getSRAM().setStatusRegister(0x00);

        //execute
        instance.execute(cpu, rdAddr, 3);
        
        //assert result
        assertEquals(0xF7, cpu.getSRAM().getRegister(rdAddr));
    }  

    /**
     * Test of execute method, of class BLD.
     */
    @Test
    public void testBLD40() {
        int rdAddr = 0;
        cpu.getSRAM().setRegister(rdAddr, 0);
        cpu.getSRAM().setStatusRegister(0x40);

        //execute
        instance.execute(cpu, rdAddr, 4);
        
        //assert result
        assertEquals(16, cpu.getSRAM().getRegister(rdAddr));
    }  

    /**
     * Test of execute method, of class BLD.
     */
    @Test
    public void testBLD41() {
        int rdAddr = 0;
        cpu.getSRAM().setRegister(rdAddr, 0xFF);
        cpu.getSRAM().setStatusRegister(0x00);

        //execute
        instance.execute(cpu, rdAddr, 4);
        
        //assert result
        assertEquals(0xEF, cpu.getSRAM().getRegister(rdAddr));
    }  

    /**
     * Test of execute method, of class BLD.
     */
    @Test
    public void testBLD50() {
        int rdAddr = 0;
        cpu.getSRAM().setRegister(rdAddr, 0);
        cpu.getSRAM().setStatusRegister(0x40);

        //execute
        instance.execute(cpu, rdAddr, 5);
        
        //assert result
        assertEquals(32, cpu.getSRAM().getRegister(rdAddr));
    }  

    /**
     * Test of execute method, of class BLD.
     */
    @Test
    public void testBLD51() {
        int rdAddr = 0;
        cpu.getSRAM().setRegister(rdAddr, 0xFF);
        cpu.getSRAM().setStatusRegister(0x00);

        //execute
        instance.execute(cpu, rdAddr, 5);
        
        //assert result
        assertEquals(0xDF, cpu.getSRAM().getRegister(rdAddr));
    }  

    /**
     * Test of execute method, of class BLD.
     */
    @Test
    public void testBLD60() {
        int rdAddr = 0;
        cpu.getSRAM().setRegister(rdAddr, 0);
        cpu.getSRAM().setStatusRegister(0x40);

        //execute
        instance.execute(cpu, rdAddr, 6);
        
        //assert result
        assertEquals(64, cpu.getSRAM().getRegister(rdAddr));
    }  

    /**
     * Test of execute method, of class BLD.
     */
    @Test
    public void testBLD61() {
        int rdAddr = 0;
        cpu.getSRAM().setRegister(rdAddr, 0xFF);
        cpu.getSRAM().setStatusRegister(0x00);

        //execute
        instance.execute(cpu, rdAddr, 6);
        
        //assert result
        assertEquals(0xBF, cpu.getSRAM().getRegister(rdAddr));
    }  

    /**
     * Test of execute method, of class BLD.
     */
    @Test
    public void testBLD70() {
        int rdAddr = 0;
        cpu.getSRAM().setRegister(rdAddr, 0);
        cpu.getSRAM().setStatusRegister(0x40);

        //execute
        instance.execute(cpu, rdAddr, 7);
        
        //assert result
        assertEquals(128, cpu.getSRAM().getRegister(rdAddr));
    }  

    /**
     * Test of execute method, of class BLD.
     */
    @Test
    public void testBLD71() {
        int rdAddr = 0;
        cpu.getSRAM().setRegister(rdAddr, 0xFF);
        cpu.getSRAM().setStatusRegister(0x00);

        //execute
        instance.execute(cpu, rdAddr, 7);
        
        //assert result
        assertEquals(0x7F, cpu.getSRAM().getRegister(rdAddr));
    }  

    
}
