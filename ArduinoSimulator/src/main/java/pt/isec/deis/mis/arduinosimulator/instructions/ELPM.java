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
import pt.isec.deis.mis.arduinosimulator.NotSupportedInstructionException;

/**
 *
 * @author pafgoncalves@ipc.pt
 */
public class ELPM extends BaseInstruction {
    
    @Override
    public void execute(CPU cpu, int rdAddr, int inc) throws Exception {
        if( cpu.getPCSize()!=22 ) {
            throw new NotSupportedInstructionException();
        }
        //isto é ao byte não word
        int addr = (cpu.getSRAM().getRegisterRAMPZ()<<16) | cpu.getSRAM().getRegisterZ();
        int value = cpu.getFLASH().get(addr/2);
        if( addr%2==1 ) {
            value = (value>>8)&0xFF;
        } else {
            value = value&0xFF;
        }
        cpu.getSRAM().setRegister(rdAddr, value);
        if( inc==1 ) {
            addr++;
            cpu.getSRAM().setRegisterZ(addr);
            cpu.getSRAM().setRegisterRAMPZ(addr>>16);
        }
        
        cpu.incPc();
        cpu.setInstructionCycles(3);
    }

    @Override
    public String getASM(CPU cpu, int rdAddr, int inc) throws Exception {
        if( cpu.getIncInDisassemble() ) {
            cpu.incPc();
        }
        return getName()+" R"+rdAddr+", Z"+(inc==1?"+":"")+"\t//Extended Load Program Memory";
    }

    @Override
    public InstructionParams decode(CPU cpu, int opcode) {
        int rdAddr = 0;
        if( (opcode&0x8)==0 ) {
            rdAddr = get5d(opcode);
        }

        params.op1 = rdAddr;
        params.op2 = opcode&1;
        return params;
    }

    @Override
    public String getName() {
        return "ELPM";
    }
    
}
