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
public class BSET extends BaseInstruction {
    
    @Override
    public void execute(CPU cpu, int flag, int op2) {
        cpu.getSRAM().setStatusRegister(cpu.getSRAM().getStatusRegister()|(1<<flag));
        cpu.incPc();
    }

    @Override
    public String getASM(CPU cpu, int flag, int op2) throws Exception {
        if( cpu.getIncInDisassemble() ) {
            cpu.incPc();
        }
        String name = getName();
        switch(flag) {
            case 0:
                name += "SEC\t//Set Carry Flag";
                break;
            case 1:
                name += "SEZ\t//Set Zero Flag";
                break;
            case 2:
                name += "SEN\t//Set Negative Flag";
                break;
            case 3:
                name += "SEV\t//Set Overflow Flag";
                break;
            case 4:
                name += "SES\t//Set Signed Flag";
                break;
            case 5:
                name += "SEH\t//Set Half Carry Flag";
                break;
            case 6:
                name += "SET\t//Set T Flag";
                break;
            case 7:
                name = "SEI\t//Enable Interrupts";
                break;
        }
        return name;
    }

    @Override
    public InstructionParams decode(CPU cpu, int opcode) {


        params.op1 = get3sbit(opcode);
        params.op2 = 0;
        return params;
    }

    @Override
    public String getName() {
        return "BSET";
    }
    
}
