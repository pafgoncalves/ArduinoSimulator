/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.isec.deis.mis.arduinosimulator.instructions;

import pt.isec.deis.mis.arduinosimulator.BaseInstruction;
import pt.isec.deis.mis.arduinosimulator.CPU;
import pt.isec.deis.mis.arduinosimulator.InstructionParams;
import pt.isec.deis.mis.arduinosimulator.utils.Utils;

/**
 *
 * @author pafgoncalves@ipc.pt
 */
public class LDI extends BaseInstruction {
    
    @Override
    public void execute(CPU cpu, int rdAddr, int K) {
        if( rdAddr<16 ) {
            throw new IllegalArgumentException("Illegal register "+rdAddr);
        }
        cpu.getSRAM().setRegister(rdAddr, K);
        cpu.incPc();
    }

    @Override
    public String getASM(CPU cpu, int rdAddr, int K) throws Exception {
        if( cpu.getIncInDisassemble() ) {
            cpu.incPc();
        }
        return getName()+" R"+rdAddr+", "+Utils.toHex(K);
    }

    @Override
    public InstructionParams decode(CPU cpu, int opcode) {
        //1110 KKKK dddd KKKK


        params.op1 = get4d(opcode);
        params.op2 = get8K(opcode);
        return params;
    }

    @Override
    public String getName() {
        return "LDI";
    }
    
}
