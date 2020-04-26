/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.isec.deis.mis.arduinosimulator.instructions;

import pt.isec.deis.mis.arduinosimulator.BaseInstruction;
import pt.isec.deis.mis.arduinosimulator.CPU;
import pt.isec.deis.mis.arduinosimulator.InstructionParams;
import pt.isec.deis.mis.arduinosimulator.InstructionSetType;

/**
 *
 * @author pafgoncalves@ipc.pt
 */
public class STY extends BaseInstruction {
    
    @Override
    public void execute(CPU cpu, int rrAddr, int type) {
        int value = cpu.getSRAM().getRegister(rrAddr);
        int addr = (cpu.getSRAM().getRegisterRAMPY()<<16) | cpu.getSRAM().getRegisterY();
        switch( type ) {
            case 0: // STY Y, Rr
                cpu.getSRAM().set(addr, value);
                if( cpu.getInstructionSetType()!=InstructionSetType.AVRxm 
                        && cpu.getInstructionSetType()!=InstructionSetType.AVRrc) {
                    cpu.setInstructionCycles(2);
                }
                break;
            case 65: // STY Y+, Rr
                cpu.getSRAM().set(addr, value);
                cpu.getSRAM().setRegisterY(addr+1);
                cpu.getSRAM().setRegisterRAMPY((addr+1)>>16);
                if( cpu.getInstructionSetType()!=InstructionSetType.AVRxm 
                        && cpu.getInstructionSetType()!=InstructionSetType.AVRrc) {
                    cpu.setInstructionCycles(2);
                }
                break;
            case 66: // STY -Y, Rr
                cpu.getSRAM().set(addr-1, value);
                cpu.getSRAM().setRegisterY(addr-1);
                cpu.getSRAM().setRegisterRAMPY((addr-1)>>16);
                cpu.setInstructionCycles(2);
                break;
            default: // STD Y+q, Rr
                cpu.getSRAM().set(addr+type, value);
                cpu.setInstructionCycles(2);
        }

        cpu.incPc();
    }

    @Override
    public String getASM(CPU cpu, int rrAddr, int type) throws Exception {
        if( cpu.getIncInDisassemble() ) {
            cpu.incPc();
        }
        switch( type ) {
            case 0:
                return getName()+" Y, R"+rrAddr+"\t//Store Indirect From Register to Data Space using Index Y";
            case 65:
                return getName()+" Y+, R"+rrAddr+"\t//Store Indirect From Register to Data Space using Index Y";
            case 66:
                return getName()+" -Y, R"+rrAddr+"\t//Store Indirect From Register to Data Space using Index Y";
            default:
                return "STD Y+"+type+", R"+rrAddr+"\t//Store Indirect From Register to Data Space using Index Y";
        }
    }

    @Override
    public InstructionParams decode(CPU cpu, int opcode) {
        params.op1 = get5d(opcode);
        if( (opcode&0x1000)==0 ) {
            int q = (opcode&0x07) | ((opcode>>7)&0x18) | ((opcode>>8)&0x20);
            params.op2 = q;
        } else {
            params.op2 = (opcode&3)+64;
        }
        return params;
    }

    @Override
    public String getName() {
        return "ST";
    }
    
}
