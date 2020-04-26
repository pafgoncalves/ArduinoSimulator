/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.isec.deis.mis.arduinosimulator;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;

/**
 *
 * @author pafgoncalves@ipc.pt
 */
public class BinaryProgramMemoryLoader extends ProgramMemoryLoader {
    
    
    @Override
    public void loadFlash(ProgramMemory pm, ByteBuffer buf) throws IOException {
        pm.resetLoadedSize();
        int addr = 0;
        while( buf.hasRemaining() ) {
            pm.set(addr++, buf.getShort()&0xFFFF);
        }
    }

    @Override
    public void loadEeprom(ProgramMemory pm, ByteBuffer buf) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
