/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.isec.deis.mis.arduinosimulatorprogrammer;

/**
 *
 * @author pafgoncalves@ipc.pt
 */
public class SyncronizeObj {

    public void doWait(long l) {
        synchronized( this ) {
            try {
                this.wait(l);
            } catch(InterruptedException e) {
            }
        }
    }

    public void doNotify() {
        synchronized( this ) {
            this.notify();
        }
    }

    public void doWait() {
        synchronized( this ) {
            try {
                this.wait();
            } catch(InterruptedException e) {
            }
        }
    }
}
