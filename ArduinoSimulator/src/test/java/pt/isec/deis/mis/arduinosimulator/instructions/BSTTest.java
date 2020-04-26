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
public class BSTTest {
    
    static CPU cpu = null;
    static BST instance = null;

    public BSTTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        cpu = new ATmega328P();
        instance = new BST();
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
     * Test of execute method, of class BST.
     */
    @Test
    public void testCopyStorageSet() {
        int rdAddr = 0;
        cpu.getSRAM().setRegister(rdAddr, 1);
        
        //execute
        instance.execute(cpu, rdAddr, 0);
        
        //assert flags
        assertEquals(true, cpu.getSRAM().getStatusRegisterObj().getCopyStorage());
    }  
   

    /**
     * Test of execute method, of class BST.
     */
    @Test
    public void testCopyStorageClear() {
        cpu.getSRAM().setStatusRegister(0xFF);
        int rdAddr = 0;
        cpu.getSRAM().setRegister(rdAddr, 0);
        
        //execute
        instance.execute(cpu, rdAddr, 0);
        
        //assert flags
        assertEquals(false, cpu.getSRAM().getStatusRegisterObj().getCopyStorage());
    }          
}
