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
public class BreakpointException extends Exception {

    private int address;
    
    public BreakpointException(int address) {
        this.address = address;
    }

    public int getBreakpointAddress() {
        return address;
    }
}
