/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.isec.deis.mis.arduinosimulatorweb;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.naming.Context;
import javax.naming.InitialContext;

/**
 *
 * @author pafgoncalves@ipc.pt
 */
public class Mail {

    private static final String from = "";

    private static final String MAIL_HEADER = "<!doctype html>\n"
            + "<html>\n"
            + "  <head>\n"
            + "   \n"
            + "    <link rel=\"stylesheet\" href=\"https://fonts.googleapis.com/css?family=Lato:400,300,700,900\">\n"
            + "  </head>\n"
            + "  <body style=\"background: #f2f2f2; text-align: center; font-family: 'Lato', sans-serif; color: #333;\">\n"
            + "    <div class=\"spacer\" style=\"height: 20px;\"></div>\n"
            + "    <div class=\"content\" style=\"margin: 4em auto 2em auto; width: 60%; max-width: 500px; background: white; padding: 3em; border-radius: 5px;\">\n"
            + "      <div class=\"logo\" style=\"border-bottom: 2px solid #eee; margin-bottom: 2em;\">\n"
            + "        <img src=\"%BASE_URL%/arduino_logo.png\" style=\"width: 40%; max-width: 300px; margin: auto;\">\n"
            + "      </div>\n";
    
    private static final String MAIL_FOOTER = "</div>\n"
            + "\n"
            + "    <footer class=\"footer\" style=\"color: #999; font-size: .8em; line-height: 1.6em;\">\n"
            + "      Arduino Simulator<br>\n"
            + "      <a href=\"%BASE_URL%\" style=\"color: #1073b9;\">%BASE_URL%</a>\n"
            + "    </footer>\n"
            + " \n"
            + "<img src=\"%BASE_URL%/users/open?id=%EMAIL%\" alt=\"\" width=\"1\" height=\"1\" border=\"0\" style=\"height:1px !important;width:1px !important;border-width:0 !important;margin-top:0 !important;margin-bottom:0 !important;margin-right:0 !important;margin-left:0 !important;padding-top:0 !important;padding-bottom:0 !important;padding-right:0 !important;padding-left:0 !important;\"/>\n"
            + "</body>\n"
            + "</html>";

    public static void sendRegisterEmail(String baseURL, String to, String uuid) throws Exception {
        sendRegisterEmail(new URL(baseURL), to, uuid);
    }
    
    public static void sendRegisterEmail(URL baseURL, String to, String uuid) throws Exception {
        String html = "      <h1 style=\"color: #E47128; font-size: 1.2em; text-transform: uppercase;\">Account verification</h1>\n"
                + "\n"
                + "<p>\n"
                + "  You recently registered an account at the\n"
                + "  <a href=\"%BASE_URL%\" style=\"color: #1073b9;\">Arduino Simulator</a> using\n"
                + "  this email address.\n"
                + "</p>\n"
                + "\n"
                + "<p>\n"
                + "  Please activate\n"
                + "  your account by clicking the button below.\n"
                + "</p>\n"
                + "\n"
                + "<p>\n"
                + "  <a class=\"button\" href=\"%BASE_URL%/validate_email?id=%UUID%&email=%EMAIL%\" style=\"background: #E47128; color: white; border-radius: 2em; padding: .8em 2em; display: inline-block; margin: 1em 0; text-decoration: none;\">Activate account</a>\n"
                + "</p>\n"
                + "\n"
                + "<p class=\"extra\" style=\"border-top: 2px solid #eee; padding-top: 2.4rem; color: #888; font-size: .8em; margin-bottom: 0;\">\n"
                + "  If you did not register an account, you can ignore this e-mail.\n"
                + "</p>";
        
        Map<String,String> vars = new HashMap<>();
        vars.put("BASE_URL", baseURL.toString());
        vars.put("UUID", uuid);
        vars.put("EMAIL", to);
        
        sendEmail(to, "Arduino Simulator account validation", html, vars);
    }

    public static void sendRecoverEmail(String baseURL, String to, String uuid) throws Exception {
        sendRecoverEmail(new URL(baseURL), to, uuid);
    }
    
    public static void sendRecoverEmail(URL baseURL, String to, String uuid) throws Exception {
        String html = "      <h1 style=\"color: #E47128; font-size: 1.2em; text-transform: uppercase;\">Password recovery</h1>\n"
                + "\n"
                + "<p>\n"
                + "  You asked to recover your password at the\n"
                + "  <a href=\"%BASE_URL%\" style=\"color: #1073b9;\">Arduino Simulator</a> using\n"
                + "  this email address.\n"
                + "</p>\n"
                + "\n"
                + "<p>\n"
                + "  To set a new password for \n"
                + "  your account click the button below.\n"
                + "</p>\n"
                + "\n"
                + "<p>\n"
                + "  <a class=\"button\" href=\"%BASE_URL%/recover_password?id=%UUID%&email=%EMAIL%\" style=\"background: #E47128; color: white; border-radius: 2em; padding: .8em 2em; display: inline-block; margin: 1em 0; text-decoration: none;\">Recover password</a>\n"
                + "</p>\n"
                + "\n"
                + "<p class=\"extra\" style=\"border-top: 2px solid #eee; padding-top: 2.4rem; color: #888; font-size: .8em; margin-bottom: 0;\">\n"
                + "  If you did not asked for a password recovery, you can ignore this e-mail.\n"
                + "</p>";
        
        Map<String,String> vars = new HashMap<>();
        vars.put("BASE_URL", baseURL.toString());
        vars.put("UUID", uuid);
        vars.put("EMAIL", to);
        
        sendEmail(to, "Arduino Simulator account validation", html, vars);
    }
    
    private static void sendEmail(String to, String subject, String body, Map<String,String> vars) throws Exception {

        Context initCtx = new InitialContext();
        Context envCtx = (Context) initCtx.lookup("java:comp/env");
        Session session = (Session) envCtx.lookup("mail/Session");

        Message message = new MimeMessage(session);

        message.setFrom(new InternetAddress(session.getProperty("mail.smtp.from")));


        InternetAddress toList[] = new InternetAddress[1];
        toList[0] = new InternetAddress(to);
        message.setRecipients(Message.RecipientType.TO, toList);

        message.setSubject(subject);

        String html = replace(MAIL_HEADER + body + MAIL_FOOTER, vars);
        message.setContent(html, "text/html;charset=UTF-8");

        Transport.send(message);

    }

    private static String replace(String str, Map<String,String> vars) {
        if( vars!=null ) {
            for(String key : vars.keySet()) {
                String search = "%"+key+"%";
                str = str.replace(search, vars.get(key));
            }
        }
        return str;
    }
}
