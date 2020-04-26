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
public class MOV extends BaseInstruction {
    
    @Override
    public void execute(CPU cpu, int rdAddr, int rrAddr) {
        cpu.getSRAM().setRegister(rdAddr, cpu.getSRAM().getRegister(rrAddr));
        cpu.incPc();
    }

    @Override
    public String getASM(CPU cpu, int rdAddr, int rrAddr) throws Exception {
        if( cpu.getIncInDisassemble() ) {
            cpu.incPc();
        }
        return getName()+" R"+rdAddr+", R"+rrAddr;
    }

    @Override
    public InstructionParams decode(CPU cpu, int opcode) {


        params.op1 = get5d(opcode);
        params.op2 = get5r(opcode);
        return params;
    }

    @Override
    public String getName() {
        return "MOV";
    }
    
}
