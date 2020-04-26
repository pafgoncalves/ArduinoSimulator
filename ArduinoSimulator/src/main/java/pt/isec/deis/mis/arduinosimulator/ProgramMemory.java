/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.isec.deis.mis.arduinosimulator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

/**
 *
 * @author pafgoncalves@ipc.pt
 */
public class ProgramMemory {

    protected final int FLASHEND;
    protected final int[] memory;
    protected final InstructionParams[] instructionParams;
    protected int maxUsed=-1;

    
    public ProgramMemory(int flashEnd) {
        FLASHEND = flashEnd;
        
        memory = new int[FLASHEND+1];
        instructionParams = new InstructionParams[FLASHEND+1];
    }
    
    public int get(int addr) {
        if( addr>maxUsed ) {
            throw new IllegalStateException("memory outside range "+addr+" (max="+maxUsed+")");
        }
        return memory[addr]&0xFFFF;
    }
    
    public void set(int addr, int value) {
        memory[addr] = value&0xFFFF;
        if( addr>maxUsed ) {
            maxUsed = addr;
        }
    }

    public InstructionParams getInstructionParams(int addr) {
        if( addr>maxUsed ) {
            throw new IllegalStateException("memory outside range "+addr+" (max="+maxUsed+")");
        }
        return instructionParams[addr];
    }

    public void setInstructionParams(int addr, InstructionParams ip) {
        instructionParams[addr] = ip;
    }
    
    public int size() {
        return FLASHEND;
    }
    
    public int loadedSize() {
        return maxUsed;
    }
    
    public void resetLoadedSize() {
        maxUsed = -1;
    }
    
    protected void loadHexFile(File file) throws IOException {
        loadHexReader(new FileReader(file));
    }
    
    protected void loadHexReader(Reader reader) throws IOException {
        maxUsed = 0;
        try (BufferedReader br = new BufferedReader(reader)) {
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

                            set(addr+i, (data_h<<8) | data_l);
                        }
                    } else if( type==1 ) {
                        break;
                    }
                }
            }
        }
    }
    
    public void dump() {
        dump(0, maxUsed+1);
    }
    
    public void dump(int start, int length) {
        for(int i=0; i<length; i++) {
            if( i%8==0 ) {
                System.out.printf("\n%04X: ", i);
            }
            System.out.printf("%04X ", get(i+start));
        }
        System.out.println();
    }
    
    public void decodeAll(CPU cpu) {
        int prevPc = cpu.getPc();
        int pc = 0;
        while( pc<=maxUsed ) {
            //é necessário por causa das intruções de 32 bits que chamam cpu.fetchNext()
            cpu.setPc(pc);
            int opcode = get(pc);
            try {
                Instruction instruction = cpu.getInstructionDecoder().getInstruction(opcode);
                InstructionParams params = new InstructionParams(instruction.decode(cpu, opcode));
                setInstructionParams(pc, params);
                pc += instruction.getSize();
            } catch(IllegalInstructionException ex) {
                setInstructionParams(pc, null);
                pc += 1;
            }
        }
        cpu.setPc(prevPc);
    }

}
