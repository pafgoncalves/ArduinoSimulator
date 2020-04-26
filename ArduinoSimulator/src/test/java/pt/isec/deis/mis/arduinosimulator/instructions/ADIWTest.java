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
public class ADIWTest {
    
    static CPU cpu = null;
    static ADIW instance = null;
    
    public ADIWTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        cpu = new ATmega328P();
        instance = new ADIW();
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
     * Test of execute method, of class ADIW.
     */
    @Test
    public void testAdd() throws Exception {
        int rdAddr = 0;
        cpu.getSRAM().setRegister(rdAddr, 1);
        
        //execute
        instance.execute(cpu, rdAddr, 1);
        
        //assert result
        assertEquals(2, cpu.getSRAM().getRegister(rdAddr));
        assertEquals(0, cpu.getSRAM().getRegister(rdAddr+1));
        
        //assert flags
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getCarry());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getZero());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getNegative());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getOverflow());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getSign());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getHalfCarry());
    }

    /**
     * Test of execute method, of class ADIW.
     */
    @Test
    public void testAdd2() throws Exception {
        int rdAddr = 0;
        cpu.getSRAM().setRegister(rdAddr, 1);
        cpu.getSRAM().setRegister(rdAddr+1, 1);
        
        //execute
        instance.execute(cpu, rdAddr, 1);
        
        //assert result
        assertEquals(2, cpu.getSRAM().getRegister(rdAddr));
        assertEquals(1, cpu.getSRAM().getRegister(rdAddr+1));
        
        //assert flags
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getCarry());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getZero());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getNegative());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getOverflow());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getSign());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getHalfCarry());
    }

    /**
     * Test of execute method, of class ADIW.
     */
    @Test
    public void testAdd3() throws Exception {
        int rdAddr = 0;
        cpu.getSRAM().setRegister(rdAddr, 1);
        
        //execute
        instance.execute(cpu, rdAddr, 256);
        
        //assert result
        assertEquals(1, cpu.getSRAM().getRegister(rdAddr));
        assertEquals(1, cpu.getSRAM().getRegister(rdAddr+1));
        
        //assert flags
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getCarry());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getZero());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getNegative());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getOverflow());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getSign());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getHalfCarry());
    }


    /**
     * Test of execute method, of class ADIW.
     */
    @Test
    public void testZero() throws Exception {
        int rdAddr = 0;
        cpu.getSRAM().setRegister(rdAddr, 1);
        
        //execute
        instance.execute(cpu, rdAddr, -1);
        
        //assert result
        assertEquals(0, cpu.getSRAM().getRegister(rdAddr));
        assertEquals(0, cpu.getSRAM().getRegister(rdAddr+1));
        
        //assert flags
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getCarry());
        assertEquals(true, cpu.getSRAM().getStatusRegisterObj().getZero());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getNegative());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getOverflow());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getSign());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getHalfCarry());
    }

   

    /**
     * Test of execute method, of class ADIW.
     */
    @Test
    public void testCarry() throws Exception {
        int rdAddr = 0;
        cpu.getSRAM().setRegister(rdAddr, 0xFF);
        cpu.getSRAM().setRegister(rdAddr+1, 0xFF);

        //execute
        instance.execute(cpu, rdAddr, 1);
        
        //assert result
        assertEquals(0, cpu.getSRAM().getRegister(rdAddr));
        assertEquals(0, cpu.getSRAM().getRegister(rdAddr+1));
        
        //assert flags
        assertEquals(true, cpu.getSRAM().getStatusRegisterObj().getCarry());
        assertEquals(true, cpu.getSRAM().getStatusRegisterObj().getZero());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getNegative());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getOverflow());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getSign());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getHalfCarry());
    }
    
    

    /**
     * Test of execute method, of class ADIW.
     */
    @Test
    public void testOverflow() throws Exception {
        int rdAddr = 0;
        cpu.getSRAM().setRegister(rdAddr, 0xFF);
        cpu.getSRAM().setRegister(rdAddr+1, 0x7F);

        //execute
        instance.execute(cpu, rdAddr, 1);
        
        //assert result
        assertEquals(0, cpu.getSRAM().getRegister(rdAddr));
        assertEquals(0x80, cpu.getSRAM().getRegister(rdAddr+1));
        
        //assert flags
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getCarry());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getZero());
        assertEquals(true, cpu.getSRAM().getStatusRegisterObj().getNegative());
        assertEquals(true, cpu.getSRAM().getStatusRegisterObj().getOverflow());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getSign());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getHalfCarry());
    }
        
}
