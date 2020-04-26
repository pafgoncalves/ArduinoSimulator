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
public class LDZ extends BaseInstruction {
    
    @Override
    public void execute(CPU cpu, int rdAddr, int type) {
        int addr = (cpu.getSRAM().getRegisterRAMPZ()<<16) | cpu.getSRAM().getRegisterZ();
        
        switch( type ) {
            case 0: // LD Rd, Z
                break;
            case 65: // LD Rd, Z+
                cpu.getSRAM().setRegisterZ(addr+1);
                if( cpu.getInstructionSetType()==InstructionSetType.AVRxm ) {
                    cpu.setInstructionCycles(1);
                } else {
                    cpu.setInstructionCycles(2);
                }
                break;
            case 66: // LD Rd, -Z
                addr = cpu.getSRAM().getRegisterZ()-1;
                cpu.getSRAM().setRegisterZ(addr);
                cpu.getSRAM().setRegisterRAMPZ(addr>>16);
                if( cpu.getInstructionSetType()==InstructionSetType.AVRxm ) {
                    cpu.setInstructionCycles(2);
                } else {
                    cpu.setInstructionCycles(3);
                }
                break;
            default:    // LD Rd, Z
                        // LDD Rd, Z+q
                addr = cpu.getSRAM().getRegisterZ()+type;
                if( cpu.getInstructionSetType()==InstructionSetType.AVRxm ) {
                    cpu.setInstructionCycles(2);
                } else {
                    cpu.setInstructionCycles(3);
                }
                
        }

        int value = cpu.getSRAM().get(addr);
        cpu.getSRAM().setRegister(rdAddr, value);

        cpu.incPc();
    }

    @Override
    public String getASM(CPU cpu, int rdAddr, int type) throws Exception {
        if( cpu.getIncInDisassemble() ) {
            cpu.incPc();
        }
        switch( type ) {
            case 0:
                return getName()+" R"+rdAddr+", Z\t//Load Indirect From Data Space to Register using Index Z";
            case 65:
                return getName()+" R"+rdAddr+", Z+\t//Load Indirect From Data Space to Register using Index Z";
            case 66:
                return getName()+" R"+rdAddr+", -Z\t//Load Indirect From Data Space to Register using Index Z";
            default:
                return "LDD R"+rdAddr+", Z+"+type+"\t//Load Indirect From Data Space to Register using Index Z";
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
        return "LD";
    }
    
}
