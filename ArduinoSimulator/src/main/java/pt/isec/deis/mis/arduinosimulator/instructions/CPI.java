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
import pt.isec.deis.mis.arduinosimulator.utils.Utils;

/**
 *
 * @author pafgoncalves@ipc.pt
 */
public class CPI extends BaseInstruction {
    
    @Override
    public void execute(CPU cpu, int rdAddr, int K) {
        DataMemory.StatusRegister status = cpu.getSRAM().getStatusRegisterObj();
        int rd = cpu.getSRAM().getRegister(rdAddr);
        
        int r = rd-K;
        
        status.setHalfCarry( notbit(rd,3)&bit(K,3) | bit(K,3)&bit(r,3) | bit(r,3)&notbit(rd,3) );
        status.setOverflow( bit(rd,7)&notbit(K,7)&notbit(r,7) | notbit(rd,7)&bit(K,7)&bit(r,7) );
        status.setNegative( bit(r,7) );
        status.setZero( (r&0xFF)==0 );
        status.setCarry( notbit(rd,7)&bit(K,7) | bit(K,7)&bit(r,7) | bit(r,7)&notbit(rd,7) );
        status.setSign( status.getNegative()!=status.getOverflow() );
        
        cpu.incPc();
    }

    @Override
    public String getASM(CPU cpu, int rdAddr, int K) throws Exception {
        if( cpu.getIncInDisassemble() ) {
            cpu.incPc();
        }
        return getName()+" R"+rdAddr+", "+Utils.toHex(K)+"\t//Compare with Immediate";
    }

    @Override
    public InstructionParams decode(CPU cpu, int opcode) {
        //CPI 0011 KKKK dddd KKKK


        params.op1 = get4d(opcode);
        params.op2 = get8K(opcode);
        return params;
    }
    
    @Override
    public String getName() {
        return "CPI";
    }
    
}
