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
import pt.isec.deis.mis.arduinosimulator.utils.Utils;

/**
 *
 * @author pafgoncalves@ipc.pt
 */
public class LDX extends BaseInstruction {
    
    @Override
    public void execute(CPU cpu, int rdAddr, int type) {
        int addr = (cpu.getSRAM().getRegisterRAMPX()<<16) | cpu.getSRAM().getRegisterX();
        
        switch( type ) {
            case 0: // LD Rd,X
                break;
            case 1: // LD Rd,X+
                cpu.getSRAM().setRegisterX(addr+1);
                if( cpu.getInstructionSetType()==InstructionSetType.AVRxm ) {
                    cpu.setInstructionCycles(1);
                } else {
                    cpu.setInstructionCycles(2);
                }
                break;
            case 2: // LD Rd,-X
                addr = cpu.getSRAM().getRegisterX()-1;
                cpu.getSRAM().setRegisterX(addr);
                cpu.getSRAM().setRegisterRAMPX(addr>>16);
                if( cpu.getInstructionSetType()==InstructionSetType.AVRxm ) {
                    cpu.setInstructionCycles(2);
                } else {
                    cpu.setInstructionCycles(3);
                }
                break;
        }

        int value = cpu.getSRAM().get(addr);
//        System.out.println("valor = "+Utils.toHex(value));
//        System.out.println("addr = "+Utils.toHex(addr,4));
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
                return getName()+" R"+rdAddr+", X\t//Load Indirect From Data Space to Register using Index X";
            case 1:
                return getName()+" R"+rdAddr+", X+\t//Load Indirect From Data Space to Register using Index X";
            case 2:
                return getName()+" R"+rdAddr+", -X\t//Load Indirect From Data Space to Register using Index X";
        }
        return getName();
    }

    @Override
    public InstructionParams decode(CPU cpu, int opcode) {

        params.op1 = get5d(opcode);
        params.op2 = opcode&3;
        return params;
    }

    @Override
    public String getName() {
        return "LD";
    }
    
}
