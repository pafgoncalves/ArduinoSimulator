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
public class SER extends BaseInstruction {
    
    @Override
    public void execute(CPU cpu, int rdAddr, int op2) {
        cpu.getSRAM().setRegister(rdAddr, 0xFF);
        cpu.incPc();
    }

    @Override
    public String getASM(CPU cpu, int rdAddr, int op2) throws Exception {
        if( cpu.getIncInDisassemble() ) {
            cpu.incPc();
        }
        return getName()+" R"+rdAddr;
    }

    @Override
    public InstructionParams decode(CPU cpu, int opcode) {
        params.op1 = get4d(opcode);
        params.op2 = 0;
        return params;
    }

    @Override
    public String getName() {
        return "SER";
    }
    
}
