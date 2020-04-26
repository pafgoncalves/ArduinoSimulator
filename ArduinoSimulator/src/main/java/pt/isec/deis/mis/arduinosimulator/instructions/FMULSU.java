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
public class FMULSU extends BaseInstruction {
    
    @Override
    public void execute(CPU cpu, int rdAddr, int rrAddr) {
        DataMemory.StatusRegister status = cpu.getSRAM().getStatusRegisterObj();
        
        int rd = cpu.getSRAM().getRegister(rdAddr);
        //mover o sinal
        rd = (byte)rd;
        
        int rr = cpu.getSRAM().getRegister(rrAddr);

        //o sinal fica no sitio correcto, não é necessário mover
        int r = rd * rr;        
        
        status.setCarry( bit(r,15) );

        r = r<<1;
        
        cpu.getSRAM().setRegister(0, r&0xFF);
        cpu.getSRAM().setRegister(1, (r>>8)&0xFF);

        status.setZero( (r&0xFFFF)==0 );
        
        cpu.incPc();
        cpu.setInstructionCycles(2);
    }

    @Override
    public String getASM(CPU cpu, int rdAddr, int rrAddr) throws Exception {
        if( cpu.getIncInDisassemble() ) {
            cpu.incPc();
        }
        return getName()+" R"+rdAddr+", R"+rrAddr;
    }

    @Override
    public InstructionParams decode(CPU cpu, int opcode) {
        params.op1 = get4d(opcode)&0x7;
        params.op2 = get4r(opcode)&0x7;
        return params;
    }

    @Override
    public String getName() {
        return "FMULSU";
    }
    
}
