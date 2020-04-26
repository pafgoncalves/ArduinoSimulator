/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.isec.deis.mis.arduinosimulator;

/**
 *
 * @author pafgoncalves@ipc.pt
 */
public abstract class BaseInstruction implements Instruction {
    protected InstructionParams params = new InstructionParams();
    
    public int fetch(CPU cpu) {
        return cpu.getFLASH().get(cpu.getPc());
    }

    protected int getNextOpcode(CPU cpu) {
        return cpu.getFLASH().get(cpu.getPc()+1);
    }
    
    /**
     * OUT 1011 1AAr rrrr AAAA
     * 
     * @param opcode
     * @return 
     */
    protected int get6A(int opcode) {
        return (((opcode>>5)&0x30) | (opcode&0xF)) + 0x20; 
    }

    /**
     * SBIS 1001 1011 AAAA Abbb
     * 
     * @param opcode
     * @return 
     */
    protected int get5A(int opcode) {
        return (opcode>>3)&0x1F;
    }

    /*
    //OUT 1011 1AAr rrrr AAAA
    //o get5d() é igual
    private int get5R(int opcode) {
        return (opcode>>4)&0x1F; 
    }
    */

    /**
     * JMP 1001 010k kkkk 110k
     *     kkkk kkkk kkkk kkkk
     *
     * @param opcode1
     * @param opcode2
     * @return 
     */
    protected int get22k(int opcode1, int opcode2) {
        int k = (opcode1>>3)&0x3E | opcode1&0x1;
        k = (k<<16)|opcode2;
        return k;
    }
    
    /**
     * RJMP 1100 kkkk kkkk kkkk
     *
     * @param opcode
     * @return 
     */
    protected int get12k(int opcode) {
        //https://stackoverflow.com/questions/34075922/convert-raw-14-bit-twos-complement-to-signed-16-bit-integer
        short k = (short)(opcode&0xFFF);
        k = (short)((short)(k<<4)/16);
        return k;
    }

    /**
     * ADD 0000 11rd dddd rrrr
     * 
     * @param opcode
     * @return 
     */
    protected int get5d(int opcode) {
        return (opcode>>4)&0x1F; 
    }

    /**
     * ADD 0000 11rd dddd rrrr
     * 
     * @param opcode
     * @return 
     */
    protected int get5r(int opcode) {
        return (((opcode>>5)&0x10) | (opcode&0xF)); 
    }
    
    /**
     * LDI 1110 KKKK dddd KKKK<br>
     * dddd é um registo de 16 a 31
     * 
     * @param opcode
     * @return 
     */
    protected int get4d(int opcode) {
        return ((opcode>>4)&0xF)+16; 
    }
    
    /**
     * MOVW 0000 0001 dddd rrrr<br>
     * rrrr é um registo 0, 2, 4, ..., 30
     * 
     * @param opcode
     * @return 
     */
    protected int get4r(int opcode) {
        return (opcode&0xF)*2; 
    }
    
    /**
     * LDI 1110 KKKK dddd KKKK
     * 
     * @param opcode
     * @return 
     */
    protected int get8K(int opcode) {
        return (opcode>>4)&0xF0 | (opcode&0xF); 
    }
    
    /**
     * BRBC 1111 01kk kkkk ksss
     * 
     * @param opcode
     * @return 
     */
    protected int get7k(int opcode) {
        byte k = (byte)((opcode>>3)&0x7F);
        k = (byte)((byte)(k<<1)/2);
//        return (opcode>>3)&0x7F; 
        return k;
    }
    
    /**
     * BRBC 1111 01kk kkkk ksss
     * 
     * @param opcode
     * @return 
     */
    protected int get3s(int opcode) {
        return opcode&0x7; 
    }

    /**
     * BSET 1001 0100 0sss 1000
     * 
     * @param opcode
     * @return 
     */
    protected int get3sbit(int opcode) {
        return (opcode>>4)&0x7; 
    }
    
    protected boolean bit(int value, int pos) {
        int mask = 1<<pos;
        return (value&mask)==mask;
    }
    
    protected boolean notbit(int value, int pos) {
        int mask = 1<<pos;
        return (value&mask)!=mask;
    }
    
    @Override
    public int getSize() {
        return 1;
    }
    
}
