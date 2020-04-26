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
public class LAC extends BaseInstruction {
    
    @Override
    public void execute(CPU cpu, int rdAddr, int op2) {

        int addr = (cpu.getSRAM().getRegisterRAMPZ()<<16) | cpu.getSRAM().getRegisterZ();
        int zvalue = cpu.getSRAM().get(addr);
        int rvalue = cpu.getSRAM().getRegister(rdAddr);
        
        cpu.getSRAM().setRegister(rdAddr, zvalue);
        cpu.getSRAM().set(addr, (0xFF-rvalue) & zvalue);
        
        cpu.incPc();
        cpu.setInstructionCycles(2);
    }

    @Override
    public String getASM(CPU cpu, int rdAddr, int op2) throws Exception {
        if( cpu.getIncInDisassemble() ) {
            cpu.incPc();
        }
        return getName()+" Z, R"+rdAddr;
    }

    @Override
    public InstructionParams decode(CPU cpu, int opcode) {
        params.op1 = get5d(opcode);
        params.op2 = 0;
        return params;
    }

    @Override
    public String getName() {
        return "LAC";
    }
    
}
