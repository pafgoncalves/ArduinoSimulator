/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.isec.deis.mis.arduinosimulator.instructions;

import pt.isec.deis.mis.arduinosimulator.BaseInstruction;
import pt.isec.deis.mis.arduinosimulator.CPU;
import pt.isec.deis.mis.arduinosimulator.InstructionParams;
import pt.isec.deis.mis.arduinosimulator.NotSupportedInstructionException;

/**
 *
 * @author pafgoncalves@ipc.pt
 */
public class EIJMP extends BaseInstruction {
    
    @Override
    public void execute(CPU cpu, int op1, int op2) throws Exception {
        if( cpu.getPCSize()!=22 ) {
            throw new NotSupportedInstructionException();
        }
        int addr = (cpu.getSRAM().getRegisterEIND()<<16) | cpu.getSRAM().getRegisterZ();
        cpu.setPc(addr);
        cpu.setInstructionCycles(2);
    }

    @Override
    public String getASM(CPU cpu, int op1, int op2) throws Exception {
        if( cpu.getIncInDisassemble() ) {
            cpu.incPc();
        }
        return getName();
    }

    @Override
    public InstructionParams decode(CPU cpu, int opcode) {
        params.op1 = 0;
        params.op2 = 0;
        return params;
    }

    @Override
    public String getName() {
        return "EIJMP";
    }
    
}
