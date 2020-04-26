/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.isec.deis.mis.arduinosimulator.instructions;

import pt.isec.deis.mis.arduinosimulator.BaseInstruction;
import pt.isec.deis.mis.arduinosimulator.CPU;
import pt.isec.deis.mis.arduinosimulator.InstructionParams;
import pt.isec.deis.mis.arduinosimulator.InstructionSetType;
import pt.isec.deis.mis.arduinosimulator.utils.Utils;

/**
 *
 * @author pafgoncalves@ipc.pt
 */
public class SBI extends BaseInstruction {
    
    @Override
    public void execute(CPU cpu, int A, int b) {

        cpu.getSRAM().setIO(A, cpu.getSRAM().getIO(A)|(1<<b));
        
        if( cpu.getInstructionSetType()!=InstructionSetType.AVRxm
                && cpu.getInstructionSetType()!=InstructionSetType.AVRrc) {
            cpu.setInstructionCycles(2);
        }
        cpu.incPc();
    }

    @Override
    public String getASM(CPU cpu, int A, int b) throws Exception {
        if( cpu.getIncInDisassemble() ) {
            cpu.incPc();
        }
        return getName()+" "+Utils.toHex(A)+","+b;
    }

    @Override
    public InstructionParams decode(CPU cpu, int opcode) {
        params.op1 = get5A(opcode);
        params.op2 = get3s(opcode);
        return params;
    }

    @Override
    public String getName() {
        return "SBI";
    }
    
}
