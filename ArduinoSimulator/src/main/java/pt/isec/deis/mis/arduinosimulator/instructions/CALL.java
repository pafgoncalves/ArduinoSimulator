/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.isec.deis.mis.arduinosimulator.instructions;

import pt.isec.deis.mis.arduinosimulator.BaseInstruction;
import pt.isec.deis.mis.arduinosimulator.CPU;
import pt.isec.deis.mis.arduinosimulator.DataMemory;
import pt.isec.deis.mis.arduinosimulator.InstructionParams;
import pt.isec.deis.mis.arduinosimulator.InstructionSetType;
import pt.isec.deis.mis.arduinosimulator.NotSupportedInstructionException;
import pt.isec.deis.mis.arduinosimulator.utils.Utils;

/**
 *
 * @author pafgoncalves@ipc.pt
 */
public class CALL extends BaseInstruction {
    
    @Override
    public void execute(CPU cpu, int k, int op2) throws Exception {
        if( cpu.getInstructionSetType()==InstructionSetType.AVRrc ) {
            throw new NotSupportedInstructionException();
        }
        
        DataMemory ram = cpu.getSRAM();
        
        if( cpu.getPCSize()==16 ) {
            ram.setStackW(ram.getStackPointer(), cpu.getPc()+2);
            ram.setStackPointer(ram.getStackPointer()-2);
        } else {
            //tem de guardar 3 bytes na stack
            ram.setStack3(ram.getStackPointer(), cpu.getPc()+2);
            ram.setStackPointer(ram.getStackPointer()-3);
        }
        
        cpu.setPc(k);

        if( cpu.getInstructionSetType()==InstructionSetType.AVRxm ) {
            if( cpu.getPCSize()==16 ) {
                cpu.setInstructionCycles(3);
            } else {
                cpu.setInstructionCycles(4);
            }
        } else {
            if( cpu.getPCSize()==16 ) {
                cpu.setInstructionCycles(4);
            } else {
                cpu.setInstructionCycles(5);
            }
        }
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
        //CALL 1001 010k kkkk 111k
        //     kkkk kkkk kkkk kkkk
        int opcode1 = opcode;
        int opcode2 = cpu.fetchNext();

        params.op1 = (opcode1>>3)&0x3E | opcode1&0x1;
        params.op1 = (params.op1<<16)| opcode2;
        return params;
    }

    @Override
    public String getName() {
        return "CALL";
    }
    
    @Override
    public int getSize() {
        return 2;
    }    
}
