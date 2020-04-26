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
public class SBIW extends BaseInstruction {
    
    @Override
    public void execute(CPU cpu, int rdAddr, int K) {
        DataMemory.StatusRegister status = cpu.getSRAM().getStatusRegisterObj();
        
        int rdl = cpu.getSRAM().getRegister(rdAddr);
        int rdh = cpu.getSRAM().getRegister(rdAddr+1);

        int r = ((rdh<<8)|rdl) - K;
        cpu.getSRAM().setRegister(rdAddr, r&0xFF);
        cpu.getSRAM().setRegister(rdAddr+1, (r>>8)&0xFF);

        status.setOverflow( notbit(rdh,7)&bit(r,15) );
        status.setNegative( bit(r,15) );
        status.setZero( (r&0xFF)==0 );
        status.setCarry( bit(r,15)&notbit(rdh,7) );
        status.setSign( status.getNegative()!=status.getOverflow() );
        
        cpu.incPc();
        cpu.setInstructionCycles(2);
    }

    @Override
    public String getASM(CPU cpu, int rdAddr, int K) throws Exception {
        if( cpu.getIncInDisassemble() ) {
            cpu.incPc();
        }
        return getName()+" R"+(rdAddr+1)+":"+rdAddr+","+K;
    }

    @Override
    public InstructionParams decode(CPU cpu, int opcode) {


        params.op1 = ((opcode>>4)&0x3) * 2 + 24;
        params.op2 = ((opcode>>2)&0x30) | (opcode&0xF);
        return params;
    }

    @Override
    public String getName() {
        return "SBIW";
    }
    
}
