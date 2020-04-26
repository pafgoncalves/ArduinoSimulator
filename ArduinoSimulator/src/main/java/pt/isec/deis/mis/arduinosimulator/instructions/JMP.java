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
public class JMP extends BaseInstruction {
    
    @Override
    public void execute(CPU cpu, int k, int op2) {
        cpu.setInstructionCycles(3);
        cpu.setPc(k);
    }

    @Override
    public String getASM(CPU cpu, int k, int op2) throws Exception {
        if( cpu.getIncInDisassemble() ) {
            cpu.setPc(cpu.getPc()+2);
        }
        return getName()+" "+Utils.toHex(k,4);
    }

    @Override
    public InstructionParams decode(CPU cpu, int opcode) {
        //JMP 1001 010k kkkk 110k   940C 1001 0100 0000 1100    000000
        //    kkkk kkkk kkkk kkkk   0062 0000 0000 0110 0010    
        int opcode1 = cpu.fetch();
        int opcode2 = cpu.fetchNext();

        params.op1 = get22k(opcode1, opcode2);
        return params;
    }

    @Override
    public String getName() {
        return "JMP";
    }
    
    @Override
    public int getSize() {
        return 2;
    }      
}
