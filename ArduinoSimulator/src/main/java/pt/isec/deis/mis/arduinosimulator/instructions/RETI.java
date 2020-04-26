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

/**
 *
 * @author pafgoncalves@ipc.pt
 */
public class RETI extends BaseInstruction {
    
    @Override
    public void execute(CPU cpu, int op1, int op2) {
        DataMemory ram = cpu.getSRAM();
        
        ram.getStatusRegisterObj().setGlobalInterruptEnable(true);
        
        int k;        
        if( cpu.getPCSize()==16 ) {
            ram.setStackPointer(ram.getStackPointer()+2);
            k = ram.getStackW(ram.getStackPointer());
        } else {
            ram.setStackPointer(ram.getStackPointer()+3);
            k = ram.getStack3(ram.getStackPointer());
        }
        
        cpu.setPc(k);
        
        if( cpu.getPCSize()==16 ) {
            cpu.setInstructionCycles(4);
        } else {
            cpu.setInstructionCycles(5);
        }
    }

    @Override
    public String getASM(CPU cpu, int op1, int op2) throws Exception {
        if( cpu.getIncInDisassemble() ) {
            cpu.incPc();
        }
        return getName();
    }

    @Override
    public InstructionParams decode(CPU cpu, int opcode) {

        return params;
    }

    @Override
    public String getName() {
        return "RETI";
    }
    
}
