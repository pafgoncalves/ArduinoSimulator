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
public class COMTest {
    
    static CPU cpu = null;
    static COM instance = null;

    public COMTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        cpu = new ATmega328P();
        instance = new COM();
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
     * Test of execute method, of class COM.
     */
    @Test
    public void testExecute() {
        int rdAddr = 0;
        cpu.getSRAM().setRegister(rdAddr, 128);

        //execute
        instance.execute(cpu, rdAddr, 0);
        
        //assert result
        assertEquals(0x7F, cpu.getSRAM().getRegister(rdAddr));
        
        //assert flags
        assertEquals(true, cpu.getSRAM().getStatusRegisterObj().getCarry());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getZero());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getNegative());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getOverflow());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getSign());
    }

    /**
     * Test of execute method, of class COM.
     */
    @Test
    public void testExecute2() {
        int rdAddr = 0;
        cpu.getSRAM().setRegister(rdAddr, 127);

        //execute
        instance.execute(cpu, rdAddr, 0);
        
        //assert result
        assertEquals(0x80, cpu.getSRAM().getRegister(rdAddr));
        
        //assert flags
        assertEquals(true, cpu.getSRAM().getStatusRegisterObj().getCarry());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getZero());
        assertEquals(true, cpu.getSRAM().getStatusRegisterObj().getNegative());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getOverflow());
        assertEquals(true, cpu.getSRAM().getStatusRegisterObj().getSign());
    }

    /**
     * Test of execute method, of class COM.
     */
    @Test
    public void testExecute3() {
        int rdAddr = 0;
        cpu.getSRAM().setRegister(rdAddr, 0);

        //execute
        instance.execute(cpu, rdAddr, 0);
        
        //assert result
        assertEquals(0xFF, cpu.getSRAM().getRegister(rdAddr));
        
        //assert flags
        assertEquals(true, cpu.getSRAM().getStatusRegisterObj().getCarry());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getZero());
        assertEquals(true, cpu.getSRAM().getStatusRegisterObj().getNegative());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getOverflow());
        assertEquals(true, cpu.getSRAM().getStatusRegisterObj().getSign());
    }

    /**
     * Test of execute method, of class COM.
     */
    @Test
    public void testExecute4() {
        int rdAddr = 0;
        cpu.getSRAM().setRegister(rdAddr, 5);

        //execute
        instance.execute(cpu, rdAddr, 0);
        
        //assert result
        assertEquals(250, cpu.getSRAM().getRegister(rdAddr));
        
        //assert flags
        assertEquals(true, cpu.getSRAM().getStatusRegisterObj().getCarry());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getZero());
        assertEquals(true, cpu.getSRAM().getStatusRegisterObj().getNegative());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getOverflow());
        assertEquals(true, cpu.getSRAM().getStatusRegisterObj().getSign());
    }

    /**
     * Test of execute method, of class COM.
     */
    @Test
    public void testExecute5() {
        int rdAddr = 0;
        cpu.getSRAM().setRegister(rdAddr, 150);

        //execute
        instance.execute(cpu, rdAddr, 0);
        
        //assert result
        assertEquals(105, cpu.getSRAM().getRegister(rdAddr));
        
        //assert flags
        assertEquals(true, cpu.getSRAM().getStatusRegisterObj().getCarry());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getZero());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getNegative());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getOverflow());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getSign());
    }


}
