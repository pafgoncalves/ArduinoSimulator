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
public class LDS extends BaseInstruction {
    
    @Override
    public void execute(CPU cpu, int rdAddr, int k) {
        cpu.getSRAM().setRegister(rdAddr, cpu.getSRAM().get((cpu.getSRAM().getRegisterRAMPD()<<16) | k));
        cpu.setPc(cpu.getPc()+2);
        cpu.setInstructionCycles(2);
    }

    @Override
    public String getASM(CPU cpu, int rdAddr, int k) throws Exception {
        if( cpu.getIncInDisassemble() ) {
            cpu.setPc(cpu.getPc()+2);
        }
        return getName()+" R"+rdAddr+", "+cpu.getRegisterName(k)+"\t//Load Direct from Data Space";
    }

    @Override
    public InstructionParams decode(CPU cpu, int opcode) {
        int opcode2 = cpu.fetchNext();

        params.op1 = get5d(opcode);
        params.op2 = opcode2;
        return params;
    }

    @Override
    public String getName() {
        return "LDS";
    }
    
    @Override
    public int getSize() {
        return 2;
    }      
}
