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
public class STS16 extends BaseInstruction {
    
    @Override
    public void execute(CPU cpu, int rrAddr, int k) {
        cpu.getSRAM().set(k, cpu.getSRAM().getRegister(rrAddr));
        cpu.incPc();
    }

    @Override
    public String getASM(CPU cpu, int rrAddr, int k) throws Exception {
        if( cpu.getIncInDisassemble() ) {
            cpu.incPc();
        }
        return getName()+" "+k+", R"+rrAddr;
    }

    @Override
    public InstructionParams decode(CPU cpu, int opcode) {
        params.op1 = get4d(opcode);
        params.op2 = (opcode&0xF) | ((opcode>>5)&0x30) | ((opcode>>2)&0x40);
        if( (opcode&0x100) == 0 ) {
            params.op2 |= 0x80;
        }
        return params;
    }

    @Override
    public String getName() {
        return "STS";
    }
    
}
