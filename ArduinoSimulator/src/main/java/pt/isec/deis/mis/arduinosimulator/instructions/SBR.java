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
public class SBR extends BaseInstruction {
    
    @Override
    public void execute(CPU cpu, int rdAddr, int K) {
        DataMemory.StatusRegister status = cpu.getSRAM().getStatusRegisterObj();

        int rd = cpu.getSRAM().getRegister(rdAddr);
        int r = rd | K;
        cpu.getSRAM().setRegister(rdAddr, r);
        
        status.setOverflow( false );
        status.setNegative( bit(r,7) );
        status.setZero( (r&0xFF)==0 );
        status.setSign( status.getNegative()!=status.getOverflow() );
        
        cpu.incPc();
    }

    @Override
    public String getASM(CPU cpu, int rdAddr, int K) throws Exception {
        if( cpu.getIncInDisassemble() ) {
            cpu.incPc();
        }
        //TODO: está a mostrar o registo errado
        return getName()+" R"+rdAddr+", "+Utils.toHex(K)+"\t//Logical OR with Immediate - SBR";
    }

    @Override
    public InstructionParams decode(CPU cpu, int opcode) {


        params.op1 = get4d(opcode);
        params.op2 = get8K(opcode);
        return params;
    }

    @Override
    public String getName() {
//        return "SBR";
        return "ORI";
    }
    
}
