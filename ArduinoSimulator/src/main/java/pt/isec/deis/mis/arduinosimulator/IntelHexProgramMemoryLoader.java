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
public class IntelHexProgramMemoryLoader extends ProgramMemoryLoader {
    
    
    @Override
    public void loadFlash(ProgramMemory pm, ByteBuffer buf) throws IOException {
        pm.resetLoadedSize();
        byte[] bytes = new byte[buf.remaining()];
        buf.get(bytes);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes)))) {
            String line;
            while( (line=br.readLine())!= null ) {
                if( line.startsWith(":") ) {
//                    System.out.println("loading "+line);
                    int size = Integer.parseInt(line.substring(1, 3),16);
                    int addr = Integer.parseInt(line.substring(3, 7),16)/2;
                    int type = Integer.parseInt(line.substring(7, 9),16);
                    if( type==0 ) {
//                        System.out.println("");
//                        for(int i=0; i<size; i+=2) {
                        for(int i=0; i<size/2; i++) {
                            int j = 9+i*4;
//                            System.out.println("j="+j);
//                            System.out.println(line.substring(j, j+2));
                            int data_l = Integer.parseInt(line.substring(j, j+2),16);
//                            System.out.print(Integer.toHexString(data_l)+" ");
                            j += 2;
//                            System.out.println(line.substring(j, j+2));
                            int data_h = Integer.parseInt(line.substring(j, j+2),16);
//                            System.out.print(Integer.toHexString(data_h)+" ");

                            pm.set(addr+i, (data_h<<8) | data_l);
                        }
                    } else if( type==1 ) {
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void loadEeprom(ProgramMemory pm, ByteBuffer buf) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
