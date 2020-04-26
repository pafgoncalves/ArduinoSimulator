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

/**
 *
 * @author pafgoncalves@ipc.pt
 */
public class SBRS extends BaseInstruction {
    
    @Override
    public void execute(CPU cpu, int rrAddr, int b) throws Exception {
        int v = cpu.getSRAM().getRegister(rrAddr);
        int mask = (1<<b);
        if( (v&mask) == mask ) {
            int opcode = cpu.fetchNext();
            Instruction nextInstruction = cpu.getInstructionDecoder().getInstruction(opcode);
            if( nextInstruction.getSize()==1 ) {
                cpu.setPc(cpu.getPc()+2);
                cpu.setInstructionCycles(2);
            } else {
                cpu.setPc(cpu.getPc()+3);
                cpu.setInstructionCycles(3);
            }
        } else {
            cpu.incPc();
        }
    }

    @Override
    public String getASM(CPU cpu, int rrAddr, int b) throws Exception {
        if( cpu.getIncInDisassemble() ) {
            cpu.incPc();
        }
        return getName()+" R"+rrAddr+", "+b+"\t//Skip if Bit in Register is Set";
    }

    @Override
    public InstructionParams decode(CPU cpu, int opcode) {
        params.op1 = get5d(opcode);
        params.op2 = get3s(opcode);
        return params;
    }

    @Override
    public String getName() {
        return "SBRS";
    }
    
}
