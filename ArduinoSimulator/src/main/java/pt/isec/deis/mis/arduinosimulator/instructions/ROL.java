/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.isec.deis.mis.arduinosimulator.instructions;

import pt.isec.deis.mis.arduinosimulator.BaseInstruction;
import pt.isec.deis.mis.arduinosimulator.CPU;
import pt.isec.deis.mis.arduinosimulator.InstructionParams;

/**
 *
 * @author pafgoncalves@ipc.pt
 */
public class ROL extends BaseInstruction {
    
    @Override
    public void execute(CPU cpu, int op1, int op2) {
        throw new IllegalStateException("Same as ADC");
    }

    @Override
    public String getASM(CPU cpu, int op1, int op2) throws Exception {
        throw new IllegalStateException("Same as ADC");
    }

    @Override
    public InstructionParams decode(CPU cpu, int opcode) {


        params.op1 = get5d(opcode);
        params.op2 = get5r(opcode);
        return params;
    }
    
    @Override
    public String getName() {
        return "ROL";
    }
    
}
