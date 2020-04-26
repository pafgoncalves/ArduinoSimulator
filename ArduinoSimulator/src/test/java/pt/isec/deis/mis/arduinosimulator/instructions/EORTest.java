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
public class EORTest {
    
    static CPU cpu = null;
    static EOR instance = null;

    public EORTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        cpu = new ATmega328P();
        instance = new EOR();
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
     * Test of execute method, of class EOR.
     */
    @Test
    public void testExecute() {
        int rdAddr = 0;
        int rrAddr = 1;
        cpu.getSRAM().setRegister(rdAddr, 0xA5);
        cpu.getSRAM().setRegister(rrAddr, 0xFF);

        //execute
        instance.execute(cpu, rdAddr, rrAddr);
        
        //assert result
        assertEquals(0x5A, cpu.getSRAM().getRegister(rdAddr));
        
        //assert flags
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getZero());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getNegative());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getOverflow());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getSign());
    }
    
    /**
     * Test of execute method, of class EOR.
     */
    @Test
    public void testExecuteNegative() {
        int rdAddr = 0;
        int rrAddr = 1;
        cpu.getSRAM().setRegister(rdAddr, 0xA5);
        cpu.getSRAM().setRegister(rrAddr, 0x5A);

        //execute
        instance.execute(cpu, rdAddr, rrAddr);
        
        //assert result
        assertEquals(0xFF, cpu.getSRAM().getRegister(rdAddr));
        
        //assert flags
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getZero());
        assertEquals(true, cpu.getSRAM().getStatusRegisterObj().getNegative());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getOverflow());
        assertEquals(true, cpu.getSRAM().getStatusRegisterObj().getSign());
    }
    
    /**
     * Test of execute method, of class EOR.
     */
    @Test
    public void testExecuteZero() {
        int rdAddr = 0;
        int rrAddr = 1;
        cpu.getSRAM().setRegister(rdAddr, 0xFF);
        cpu.getSRAM().setRegister(rrAddr, 0xFF);

        //execute
        instance.execute(cpu, rdAddr, rrAddr);
        
        //assert result
        assertEquals(0x00, cpu.getSRAM().getRegister(rdAddr));
        
        //assert flags
        assertEquals(true, cpu.getSRAM().getStatusRegisterObj().getZero());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getNegative());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getOverflow());
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getSign());
    }
    
}
