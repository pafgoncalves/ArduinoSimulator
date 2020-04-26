/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.isec.deis.mis.arduinosimulator;

/**
 *
 * @author pafgoncalves@ipc.pt
 */
public enum InstructionSetType {
    
    AVR("AT90", "Original instruction set from 1995."),
    AVRe("megaAVR", "Multiply (xMULxx), Move Word (MOVW), and enhanced Load Program Memory (LPM) added to the AVR instruction set. No timing differences."),
    AVRxm("XMEGA", "Significantly different timing compared to AVR(e). The Read Modify Write (RMW) and DES encryption instructions are unique to this version."),
    AVRxt("(AVR)", "AVR 2016 and onwards. This variant is based on AVRe and AVRxm. Closer related to AVRe, but with improved timing."),
    AVRrc("tinyAVR", "The Reduced Core AVR CPU was developed for ultra-low pinout (6-pin) size constrained devices. The AVRrc therefore only has a 16 registers register-file (R31-R16) and a limited instruction set.");
    
    private final String device;
    private final String desc;
    
    private InstructionSetType(String device, String desc) {
        this.device = device;
        this.desc = desc;
    }
    
    public String getDevice() {
        return device;
    }

    public String getDescription() {
        return desc;
    }
    
    @Override
    public String toString() {
        return device+" - "+desc;
    }
}
