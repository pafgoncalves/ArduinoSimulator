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
public class SBC extends BaseInstruction {
    
    @Override
    public void execute(CPU cpu, int rdAddr, int rrAddr) {
        DataMemory.StatusRegister status = cpu.getSRAM().getStatusRegisterObj();
        
        int rd = cpu.getSRAM().getRegister(rdAddr);
        int rr = cpu.getSRAM().getRegister(rrAddr);

        //TODO: bit negativo não vai ficar onde esperado
        int r = rd - rr - (status.getCarry()?1:0);        
        cpu.getSRAM().setRegister(rdAddr, r);
//System.out.println("SBC: "+r+" = "+rd+" - "+rr+" - "+(status.getCarry()?1:0));
        status.setHalfCarry( notbit(rd,3)&bit(rr,3) | bit(rr,3)&bit(r,3) | bit(r,3)&notbit(rd,3) );
        status.setOverflow( bit(rd,7)&notbit(rr,7)&notbit(r,7) | notbit(rd,7)&bit(rr,7)&bit(r,7) );
        status.setNegative( bit(r,7) );
        status.setZero( r==0 & status.getZero() );
        status.setCarry( notbit(rd,7)&bit(rr,7) | bit(rr,7)&bit(r,7) | bit(r,7)&notbit(rd,7) );
        status.setSign( status.getNegative()!=status.getOverflow() );
        
        cpu.incPc();
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

        params.op1 = get5d(opcode);
        params.op2 = get5r(opcode);
        return params;
    }

    @Override
    public String getName() {
        return "SBC";
    }
    
}
