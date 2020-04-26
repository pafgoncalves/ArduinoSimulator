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
public class CBITest {
    
    static CPU cpu = null;
    static CBI instance = null;

    public CBITest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        cpu = new ATmega328P();
        instance = new CBI();
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
     * Test of execute method, of class CBI.
     */
    @Test
    public void testCBI() {
        int ioAddr = 0;

        cpu.getSRAM().setIO(ioAddr, 0xFF);
        
        //execute
        instance.execute(cpu, ioAddr, 0);
        
        //assert result
        assertEquals(0xFE, cpu.getSRAM().getIO(ioAddr));
    }


    @Test
    public void testCBI2() {
        int ioAddr = 31;

        cpu.getSRAM().setIO(ioAddr, 0xFF);
        
        //execute
        instance.execute(cpu, ioAddr, 7);
        
        //assert result
        assertEquals(0x7F, cpu.getSRAM().getIO(ioAddr));
    }
    
}
