/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.isec.deis.mis.arduinosimulator.instructions;

import pt.isec.deis.mis.arduinosimulator.BaseInstruction;
import pt.isec.deis.mis.arduinosimulator.BreakpointException;
import pt.isec.deis.mis.arduinosimulator.CPU;
import pt.isec.deis.mis.arduinosimulator.Instruction;
import pt.isec.deis.mis.arduinosimulator.InstructionParams;

/**
 *
 * @author pafgoncalves@ipc.pt
 */
public class BREAK extends BaseInstruction {
    
    private int instructionValue = Integer.MIN_VALUE;
    private boolean inBreak = false;
    
    public BREAK() {
    }
    
    public BREAK(int instructionValue) {
        this.instructionValue = instructionValue;
    }
    
    public int getPreviousIntructionValue() {
        return instructionValue;
    }
    
    @Override
    public void execute(CPU cpu, int op1, int op2) throws Exception {
        //os parametros são os da instrução original
        if( inBreak==false ) {
            inBreak = true;
            throw new BreakpointException(cpu.getPc());
        } else {
            inBreak = false;
            if( instructionValue != Integer.MIN_VALUE ) {
                Instruction instruction = cpu.getInstructionDecoder().getInstruction(instructionValue);
                instruction.execute(cpu, op1, op2);
            } else {
                //é um break que existia no código original
                cpu.incPc();
            }
        }
    }

    @Override
    public String getASM(CPU cpu, int op1, int op2) throws Exception {
        //os parametros são os da instrução original
        if( instructionValue != Integer.MIN_VALUE ) {
            Instruction instruction = cpu.getInstructionDecoder().getInstruction(instructionValue);
            //return getName()+" on "+instruction.getASM(cpu, op1, op2);
            //o cliente trata da indicação se é ou não um BREAK
            return instruction.getASM(cpu, op1, op2);
        } else {
            if( cpu.getIncInDisassemble() ) {
                cpu.incPc();
            }
            return getName();
        }
    }

    @Override
    public InstructionParams decode(CPU cpu, int opcode) {
        return params;
    }

    @Override
    public String getName() {
        return "BREAK";
    }
    
}
