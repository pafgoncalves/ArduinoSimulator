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
public class OUT extends BaseInstruction {
    
    @Override
    public void execute(CPU cpu, int rrAddr, int A) {
        int value = cpu.getSRAM().getRegister(rrAddr);
        cpu.getSRAM().set(A, value);
        cpu.incPc();
    }

    @Override
    public String getASM(CPU cpu, int rrAddr, int A) throws Exception {
        if( cpu.getIncInDisassemble() ) {
            cpu.incPc();
        }
        return getName()+" "+cpu.getRegisterName(A)+", R"+rrAddr;
    }

    @Override
    public InstructionParams decode(CPU cpu, int opcode) {
        //1011 1AAr rrrr AAAA


        params.op1 = get5d(opcode);
        params.op2 = get6A(opcode);
        return params;
    }

    @Override
    public String getName() {
        return "OUT";
    }
    
}
