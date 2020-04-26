/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.isec.deis.mis.arduinosimulator.utils;

/**
 *
 * @author pafgoncalves@ipc.pt
 */
public class Utils {
    
    
    public static String toHex(int value) {
        return toHex(value, 2);
    }
    
    public static String toHex(int value, int size) {
        String str = Integer.toHexString(value).toUpperCase();
        while( str.length()<size ) {
            str = "0"+str;
        }
        return "0x"+str;
    }
 
    
    public static int parseInt(String str) {
        if( str.startsWith("0x") || str.startsWith("0X") ) {
            return Integer.parseInt(str.substring(2), 16);
        }
        return Integer.parseInt(str);
    }
    
    public static void printStackTrace() {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        for(int i=2; i<stack.length; i++) {
            System.out.println(stack[i]);
        }
    }
}
