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
public class BCLR extends BaseInstruction {
    
    @Override
    public void execute(CPU cpu, int s, int op2) {
        /*
        DataMemory.StatusRegister status = cpu.getSRAM().getStatusRegisterObj();
        switch( s ) {
            case 0:
                status.setCarry(false);
                break;
            case 1:
                status.setZero(false);
                break;
            case 2:
                status.setNegative(false);
                break;
            case 3:
                status.setOverflow(false);
                break;
            case 4:
                status.setSign(false);
                break;
            case 5:
                status.setHalfCarry(false);
                break;
            case 6:
                status.setCopyStorage(false);
                break;
            case 7:
                status.setGlobalInterruptEnable(false);
                break;
        }*/
        
        cpu.getSRAM().setStatusRegister(cpu.getSRAM().getStatusRegister()&(~(1<<s)));
        cpu.incPc();
    }

    @Override
    public String getASM(CPU cpu, int s, int op2) throws Exception {
        if( cpu.getIncInDisassemble() ) {
            cpu.incPc();
        }
        String name = getName();
        switch(s) {
            case 0:
                name += "CLC\t//Clear Carry Flag";
                break;
            case 1:
                name += "CLZ\t//Clear Zero Flag";
                break;
            case 2:
                name += "CLN\t//Clear Negative Flag";
                break;
            case 3:
                name += "CLV\t//Clear Overflow Flag";
                break;
            case 4:
                name += "CLS\t//Clear Signed Flag";
                break;
            case 5:
                name += "CLH\t//Clear Half Carry Flag";
                break;
            case 6:
                name += "CLT\t//Clear T Flag";
                break;
            case 7:
                name = "CLI\t//Disable Interrupts";
                break;
        }
        return name;
//        return getName()+" "+s+"\t//Bit Clear in SREG";
    }

    @Override
    public InstructionParams decode(CPU cpu, int opcode) {


        params.op1 = get3sbit(opcode);
        params.op2 = 0;
        return params;
    }
    
    @Override
    public String getName() {
        return "BCLR";
    }
    
}
