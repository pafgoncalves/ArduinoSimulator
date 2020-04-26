/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.isec.deis.mis.arduinosimulator.peripherals;

/**
 *
 * @author pafgoncalves@ipc.pt
 */
public interface Usart {
    
    public void write(char c);
    
    public void addListener(UsartListener l);
    public void removeListener(UsartListener l);
    
    public interface UsartListener {
        public void onChar(char c);
    }
    
}
