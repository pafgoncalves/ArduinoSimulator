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
public class IllegalInstructionException extends Exception {

    private int opcode;
    
    public IllegalInstructionException(int opcode) {
        this.opcode = opcode;
    }
    
    public int getOpcode() {
        return opcode;
    }
    
}
