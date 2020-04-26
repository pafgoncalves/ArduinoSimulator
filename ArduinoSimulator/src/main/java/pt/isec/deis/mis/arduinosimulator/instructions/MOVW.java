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
public class MOVW extends BaseInstruction {
    
    @Override
    public void execute(CPU cpu, int rdAddr, int rrAddr) {
        int rr = cpu.getSRAM().getRegister(rrAddr);
        cpu.getSRAM().setRegister(rdAddr, rr);

        int rr1 = cpu.getSRAM().getRegister(rrAddr+1);
        cpu.getSRAM().setRegister(rdAddr+1, rr1);
        
        cpu.incPc();
    }

    @Override
    public String getASM(CPU cpu, int rdAddr, int rrAddr) throws Exception {
        int opcode = fetch(cpu);
        if( cpu.getIncInDisassemble() ) {
            cpu.incPc();
        }
        return getName()+" R"+(rdAddr+1)+":"+rdAddr+", R"+(rrAddr+1)+":"+rrAddr;
    }

    @Override
    public InstructionParams decode(CPU cpu, int opcode) {


        params.op1 = (get4d(opcode)-16)*2;
        params.op2 = get4r(opcode);
        return params;
    }

    @Override
    public String getName() {
        return "MOVW";
    }
    
}
