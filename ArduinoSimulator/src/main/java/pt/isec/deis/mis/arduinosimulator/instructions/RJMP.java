/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.isec.deis.mis.arduinosimulator.instructions;

import pt.isec.deis.mis.arduinosimulator.BaseInstruction;
import pt.isec.deis.mis.arduinosimulator.CPU;
import pt.isec.deis.mis.arduinosimulator.InstructionParams;
import pt.isec.deis.mis.arduinosimulator.utils.Utils;

/**
 *
 * @author pafgoncalves@ipc.pt
 */
public class RJMP extends BaseInstruction {
    
    @Override
    public void execute(CPU cpu, int k, int op2) {
        cpu.incPc();
        cpu.setPc(cpu.getPc()+k);
        cpu.setInstructionCycles(2);
    }

    @Override
    public String getASM(CPU cpu, int k, int op2) throws Exception {
        int pc = 0;
        if( cpu.getIncInDisassemble() ) {
            cpu.incPc();
            pc = cpu.getPc();
        } else {
            pc = cpu.getPc()+1;
        }
        //só a seguir a incrementar é que soma ao PC o valor
        return getName()+" "+k+"\t//"+Utils.toHex(pc+k,4);
    }

    @Override
    public InstructionParams decode(CPU cpu, int opcode) {
        //1100 kkkk kkkk kkkk


        params.op1 = get12k(opcode);
        return params;
    }

    @Override
    public String getName() {
        return "RJMP";
    }
    
}
