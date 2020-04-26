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
public class NEG extends BaseInstruction {
    
    @Override
    public void execute(CPU cpu, int rdAddr, int op2) {
        DataMemory.StatusRegister status = cpu.getSRAM().getStatusRegisterObj();
//System.out.println("----------------------------------");
        int rd = cpu.getSRAM().getRegister(rdAddr);
//System.out.println("rd = "+Utils.toHex(rd));

        int r = (0 - rd) & 0xFF;
//        int r = ((~rd)&0xFF)+1;
System.out.println("r = "+Utils.toHex(r));
//System.out.println("----------------------------------");
        cpu.getSRAM().setRegister(rdAddr, r);

        status.setHalfCarry( bit(r,3) | notbit(rd,3) );
        status.setOverflow( r==0x80 );
        status.setNegative( bit(r,7) );
        status.setZero( (r&0xFF)==0 );
        status.setCarry( r!=0 );
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
        return "NEG";
    }
    
}
