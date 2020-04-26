/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.isec.deis.mis.arduinosimulatorweb;

import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author pafgoncalves@ipc.pt
 */
public class Utils {
    
    private Utils() {}
    
    public static String getAppURL(HttpServletRequest request) {
        String scheme = request.getScheme();
        int port = request.getServerPort();
        String appPath = scheme + "://" + request.getServerName();
        if( 
                (!scheme.equalsIgnoreCase("http") || port!=80 )
                &&
                (!scheme.equalsIgnoreCase("https") || port!=443 )
                ) {
            appPath += ":"+port;
        }
        appPath += request.getContextPath();
        return appPath;
    }
}
