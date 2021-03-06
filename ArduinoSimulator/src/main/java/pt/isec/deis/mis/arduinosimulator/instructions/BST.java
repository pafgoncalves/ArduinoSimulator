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
public class BST extends BaseInstruction {
    
    @Override
    public void execute(CPU cpu, int rdAddr, int b) {
        DataMemory.StatusRegister status = cpu.getSRAM().getStatusRegisterObj();
        int rd = cpu.getSRAM().getRegister(rdAddr);
        int mask = 1<<b;
        status.setCopyStorage( ((rd&mask)==mask) );

        cpu.incPc();
    }

    @Override
    public String getASM(CPU cpu, int rdAddr, int b) throws Exception {
        if( cpu.getIncInDisassemble() ) {
            cpu.incPc();
        }
        return getName()+" R"+rdAddr+","+b;
    }

    @Override
    public InstructionParams decode(CPU cpu, int opcode) {


        params.op1 = get5d(opcode);
        params.op2 = get3s(opcode);
        return params;
    }

    @Override
    public String getName() {
        return "BST";
    }
    
}
