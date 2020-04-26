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
public class DEC extends BaseInstruction {
    
    @Override
    public void execute(CPU cpu, int rdAddr, int op2) {
        DataMemory.StatusRegister status = cpu.getSRAM().getStatusRegisterObj();

        int rd = cpu.getSRAM().getRegister(rdAddr);

        //o bit negativo não vai ficar no sitio certo com aritemetica a 32 bits
        //mas como o valor que se subtrai é sempre 1 não há problema
        int r = rd-1;
        cpu.getSRAM().setRegister(rdAddr, r);
        
        status.setOverflow( notbit(r,7)&bit(r,6)&bit(r,5)&bit(r,4)&bit(r,3)&bit(r,2)&bit(r,1)&bit(r,0) );
        status.setNegative( bit(r,7) );
        status.setZero( (r&0xFF)==0 );
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
        return "DEC";
    }
    
}
