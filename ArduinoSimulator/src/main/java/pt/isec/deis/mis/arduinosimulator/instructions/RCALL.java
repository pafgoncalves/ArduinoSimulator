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
import pt.isec.deis.mis.arduinosimulator.utils.Utils;

/**
 *
 * @author pafgoncalves@ipc.pt
 */
public class RCALL extends BaseInstruction {
    
    @Override
    public void execute(CPU cpu, int k, int op2) {
        DataMemory ram = cpu.getSRAM();
        
        if( cpu.getPCSize()==16 ) {
            ram.setStackW(ram.getStackPointer(), cpu.getPc()+1);
            ram.setStackPointer(ram.getStackPointer()-2);
        } else {
            //tem de guardar 3 bytes na stack
            ram.setStack3(ram.getStackPointer(), cpu.getPc()+1);
            ram.setStackPointer(ram.getStackPointer()-3);
        }
        
        cpu.setPc(cpu.getPc()+k+1);

        if( cpu.getInstructionSetType()==InstructionSetType.AVRxm ) {
            if( cpu.getPCSize()==16 ) {
                cpu.setInstructionCycles(2);
            } else {
                cpu.setInstructionCycles(3);
            }
        } else {
            if( cpu.getPCSize()==16 ) {
                cpu.setInstructionCycles(3);
            } else {
                cpu.setInstructionCycles(4);
            }
        }
        
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
        params.op1 = get12k(opcode);
        params.op2 = 0;
        return params;
    }

    @Override
    public String getName() {
        return "RCALL";
    }
    
}
