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
public class InstructionParams {
    public int op1;
    public int op2;

    public InstructionParams() {
    }

    public InstructionParams(InstructionParams ip) {
        this.op1 = ip.op1;
        this.op2 = ip.op2;
    }
    
    
}
