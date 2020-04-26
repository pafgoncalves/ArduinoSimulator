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
public class SUB extends BaseInstruction {
    
    @Override
    public void execute(CPU cpu, int rdAddr, int rrAddr) {
        DataMemory.StatusRegister status = cpu.getSRAM().getStatusRegisterObj();
        
        int rd = cpu.getSRAM().getRegister(rdAddr);
        int rr = cpu.getSRAM().getRegister(rrAddr);

        //bit negativo não vai ficar onde esperado?
        //fica sim. validado com o seguinto código no hardware real:
        /*
            char flags;
            register int8_t value asm("r20");
            asm volatile("LDI R20, -128" ::);
            asm volatile("LDI R21, 1" ::);
            asm volatile("SUB R20, R21" ::);
            asm volatile("in %0, %1" : "=r" (flags) : "I" (_SFR_IO_ADDR(SREG)));
            Serial.println(value, HEX);
            Serial.println(flags, BIN);
        */
        int r = rd - rr;        
        cpu.getSRAM().setRegister(rdAddr, r);

        status.setHalfCarry( notbit(rd,3)&bit(rr,3) | bit(rr,3)&bit(r,3) | bit(r,3)&notbit(rd,3) );
        status.setOverflow( bit(rd,7)&notbit(rr,7)&notbit(r,7) | notbit(rd,7)&bit(rr,7)&bit(r,7) );
        status.setNegative( bit(r,7) );
        status.setZero( (r&0xFF)==0 );
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
        return "SUB";
    }
    
}
