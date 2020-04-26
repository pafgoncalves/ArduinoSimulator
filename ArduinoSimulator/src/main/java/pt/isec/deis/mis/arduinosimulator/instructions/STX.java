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
public class STX extends BaseInstruction {
    
    @Override
    public void execute(CPU cpu, int rrAddr, int type) {
        int value = cpu.getSRAM().getRegister(rrAddr);
        int addr = (cpu.getSRAM().getRegisterRAMPX()<<16) | cpu.getSRAM().getRegisterX();
        switch( type ) {
            case 0: // STX X,Rr
                cpu.getSRAM().set(addr, value);
                if( cpu.getInstructionSetType()!=InstructionSetType.AVRxm 
                        && cpu.getInstructionSetType()!=InstructionSetType.AVRrc) {
                    cpu.setInstructionCycles(2);
                }
                break;
            case 1: // STX X+,Rr
                cpu.getSRAM().set(addr, value);
                cpu.getSRAM().setRegisterX(addr+1);
                cpu.getSRAM().setRegisterRAMPX((addr+1)>>16);
                if( cpu.getInstructionSetType()!=InstructionSetType.AVRxm 
                        && cpu.getInstructionSetType()!=InstructionSetType.AVRrc) {
                    cpu.setInstructionCycles(2);
                }
                break;
            case 2: // STX -X,Rr
                cpu.getSRAM().set(addr-1, value);
                cpu.getSRAM().setRegisterX(addr-1);
                cpu.getSRAM().setRegisterRAMPX((addr-1)>>16);
                cpu.setInstructionCycles(2);
                break;
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
                return getName()+" X, R"+rrAddr+"\t//Store Indirect From Register to Data Space using Index X";
            case 1:
                return getName()+" X+, R"+rrAddr+"\t//Store Indirect From Register to Data Space using Index X";
            case 2:
                return getName()+" -X, R"+rrAddr+"\t//Store Indirect From Register to Data Space using Index X";
        }
        return getName();
    }

    @Override
    public InstructionParams decode(CPU cpu, int opcode) {
        //(i)   1001 001r rrrr 1100
        //(ii)  1001 001r rrrr 1101
        //(iii) 1001 001r rrrr 1110
        params.op1 = get5d(opcode);
        params.op2 = opcode&3;
        return params;
    }

    @Override
    public String getName() {
        return "ST";
    }
    
}
