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
public class BRBS extends BaseInstruction {
    
    @Override
    public void execute(CPU cpu, int flag, int k) {
        int sreg = cpu.getSRAM().getStatusRegister();
        if( (sreg&(1<<flag)) != 0 ) {
            //branch
            cpu.setPc(cpu.getPc()+k+1);
            cpu.setInstructionCycles(2);
        } else {
            cpu.incPc();
        }
    }

    @Override
    public String getASM(CPU cpu, int flag, int k) throws Exception {
        int pc = 0;
        if( cpu.getIncInDisassemble() ) {
            cpu.incPc();
            pc = cpu.getPc();
        } else {
            pc = cpu.getPc()+1;
        }
        String name = getName();
        switch(flag) {
            case 0:
                name = "BRLO";
                break;
            case 1:
                name = "BREQ";
                break;
            case 2:
                name = "BRMI";
                break;
            case 3:
                name = "BRVS";
                break;
            case 4:
                name = "BRLT";
                break;
            case 5:
                name = "BRHS";
                break;
            case 6:
                name = "BRTS";
                break;
            case 7:
                name = "BRIE";
                break;
        }
        return name+" "+k+"\t//"+Utils.toHex(pc+k,4);
    }

    @Override
    public InstructionParams decode(CPU cpu, int opcode) {

        params.op1 = get3s(opcode);
        params.op2 = get7k(opcode);
        return params;
    }

    @Override
    public String getName() {
        return "BRBS";
    }
    
}
