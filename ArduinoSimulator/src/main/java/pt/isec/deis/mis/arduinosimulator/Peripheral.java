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
public interface Peripheral {
    
    public void clock();
    public void start();
    public void stop();
    public void reset();
    
}
