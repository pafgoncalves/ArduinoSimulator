/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.isec.deis.mis.arduinosimulator.instructions;

import pt.isec.deis.mis.arduinosimulator.BaseInstruction;
import pt.isec.deis.mis.arduinosimulator.CPU;
import pt.isec.deis.mis.arduinosimulator.InstructionParams;

/**
 *
 * @author pafgoncalves@ipc.pt
 */
public class IN extends BaseInstruction {
    
    @Override
    public void execute(CPU cpu, int rdAddr, int A) {
        cpu.getSRAM().setRegister(rdAddr, cpu.getSRAM().get(A));
        cpu.incPc();
    }

    @Override
    public String getASM(CPU cpu, int rdAddr, int A) throws Exception {
        if( cpu.getIncInDisassemble() ) {
            cpu.incPc();
        }
        return getName()+" R"+rdAddr+", "+cpu.getRegisterName(A);
    }

    @Override
    public InstructionParams decode(CPU cpu, int opcode) {


        params.op1 = get5d(opcode);
        params.op2 = get6A(opcode);
        return params;
    }

    @Override
    public String getName() {
        return "IN";
    }
    
}
