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
public class ROR extends BaseInstruction {
    
    @Override
    public void execute(CPU cpu, int rdAddr, int op2) {
        DataMemory.StatusRegister status = cpu.getSRAM().getStatusRegisterObj();
        
        int rd = cpu.getSRAM().getRegister(rdAddr);

        int r = rd>>1;
        if( status.getCarry() ) {
            r = r | 0x80;
        } else {
            r = r & 0x7F;
        }
        cpu.getSRAM().setRegister(rdAddr, r);

        status.setNegative( bit(r,7) );
        status.setZero( (r&0xFF)==0 );
        status.setCarry( bit(rd,0) );
        status.setOverflow( status.getNegative()!=status.getCarry());
        status.setSign( status.getNegative()!=status.getOverflow() );
        
        cpu.incPc();
    }

    @Override
    public String getASM(CPU cpu, int rdAddr, int op2) throws Exception {
        if( cpu.getIncInDisassemble() ) {
            cpu.incPc();
        }
        return getName()+" R"+rdAddr;
    }

    @Override
    public InstructionParams decode(CPU cpu, int opcode) {
        params.op1 = get5d(opcode);
        params.op2 = 0;
        return params;
    }

    @Override
    public String getName() {
        return "ROR";
    }
    
}
