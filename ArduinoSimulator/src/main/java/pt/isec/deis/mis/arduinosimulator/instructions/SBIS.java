/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.isec.deis.mis.arduinosimulator.instructions;

import pt.isec.deis.mis.arduinosimulator.BaseInstruction;
import pt.isec.deis.mis.arduinosimulator.CPU;
import pt.isec.deis.mis.arduinosimulator.IllegalInstructionException;
import pt.isec.deis.mis.arduinosimulator.Instruction;
import pt.isec.deis.mis.arduinosimulator.InstructionParams;
import pt.isec.deis.mis.arduinosimulator.InstructionSetType;

/**
 *
 * @author pafgoncalves@ipc.pt
 */
public class SBIS extends BaseInstruction {
    
    @Override
    public void execute(CPU cpu, int A, int b) throws Exception {
        int v = cpu.getSRAM().getIO(A);
        int mask = (1<<b);
        //If I/O(A,b) = 1 then PC ← PC + 2 (or 3) else PC ← PC + 1
        if( (v&mask) == mask ) {
            int opcode = cpu.fetchNext();
            Instruction nextInstruction = cpu.getInstructionDecoder().getInstruction(opcode);
            if( nextInstruction.getSize()==1 ) {
                cpu.setPc(cpu.getPc()+2);
                if( cpu.getInstructionSetType()==InstructionSetType.AVRxm ) {
                    cpu.setInstructionCycles(3);
                } else {
                    cpu.setInstructionCycles(2);
                }
            } else {
                cpu.setPc(cpu.getPc()+3);
                if( cpu.getInstructionSetType()==InstructionSetType.AVRxm ) {
                    cpu.setInstructionCycles(4);
                } else {
                    cpu.setInstructionCycles(3);
                }
            }
        } else {
            cpu.incPc();
            if( cpu.getInstructionSetType()==InstructionSetType.AVRxm ) {
                cpu.setInstructionCycles(2);
            }
        }
        
    }

    @Override
    public String getASM(CPU cpu, int A, int b) throws Exception {
        if( cpu.getIncInDisassemble() ) {
            cpu.incPc();
        }
        return getName()+" "+cpu.getRegisterName(A+0x20)+", "+b+"\t//Skip if Bit in I/O Register is Set";
    }

    @Override
    public InstructionParams decode(CPU cpu, int opcode) {
        params.op1 = get5A(opcode);
        params.op2 = get3s(opcode);
        return params;
    }

    @Override
    public String getName() {
        return "SBIS";
    }
    
}
