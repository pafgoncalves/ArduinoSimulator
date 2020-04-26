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
public class NotSupportedInstructionException extends Exception {

    /**
     * Creates a new instance of <code>NotSupportedInstructionException</code>
     * without detail message.
     */
    public NotSupportedInstructionException() {
    }

    /**
     * Constructs an instance of <code>NotSupportedInstructionException</code>
     * with the specified detail message.
     *
     * @param msg the detail message.
     */
    public NotSupportedInstructionException(String msg) {
        super(msg);
    }
}
