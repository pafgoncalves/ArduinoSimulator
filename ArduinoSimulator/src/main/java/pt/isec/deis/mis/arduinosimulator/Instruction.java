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
public interface Instruction {
    public void execute(CPU cpu, int op1, int op2) throws Exception;
    public String getASM(CPU cpu, int op1, int op2) throws Exception;
    public InstructionParams decode(CPU cpu, int opcode);
    public String getName();
    public int getSize();
}
