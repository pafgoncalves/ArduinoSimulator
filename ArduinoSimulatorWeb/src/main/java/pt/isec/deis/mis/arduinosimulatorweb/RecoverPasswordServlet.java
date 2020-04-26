/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.isec.deis.mis.arduinosimulatorweb;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author pafgoncalves@ipc.pt
 */
@WebServlet("/recover_password")
public class RecoverPasswordServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if( request.getParameter("id")!=null ) {
            String verify = verifyPasswordDate(request);
            if( verify!=null ) {
                request.setAttribute("changePasswordError", verify);
                request.getRequestDispatcher("/WEB-INF/recover_change_password_nok.jsp").forward(request, response);
            } else {
                request.setAttribute("id", request.getParameter("id"));
                request.setAttribute("email", request.getParameter("email"));
                request.getRequestDispatcher("/WEB-INF/recover_change_password.jsp").forward(request, response);
            }
        } else {
            request.getRequestDispatcher("/WEB-INF/recover_password.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        if( request.getParameter("password")!=null ) {
            
            String password = request.getParameter("password");
            String password2 = request.getParameter("password2");
            Map<String, String> messages = new HashMap<>();

            if( password == null || password.isEmpty() ) {
                messages.put("password", "Please enter password");
            }

            if( password2 == null || password2.isEmpty() ) {
                messages.put("password2", "Please enter password");
            }

            if( password != null && !password.equals(password2) ) {
                messages.put("password2", "Passwords don't match");
            }
            
            if( messages.isEmpty() ) {

                String changePasswordError = changePassword(request);

                if( changePasswordError==null ) {
                    request.getRequestDispatcher("/WEB-INF/recover_change_password_ok.jsp").forward(request, response);
                    return;
                }

                request.setAttribute("changePasswordError", changePasswordError);
                request.getRequestDispatcher("/WEB-INF/recover_change_password_nok.jsp").forward(request, response);
            } else {
                request.setAttribute("messages", messages);
                request.setAttribute("id", request.getParameter("id"));
                request.setAttribute("email", request.getParameter("email"));
                request.getRequestDispatcher("/WEB-INF/recover_change_password.jsp").forward(request, response);
            }
        } else {
        
            String recoverError = sendRecoverEmail(request);

            if( recoverError==null ) {
                request.getRequestDispatcher("/WEB-INF/recover_password_ok.jsp").forward(request, response);
                return;
            }

            request.setAttribute("recoverError", recoverError);
            request.getRequestDispatcher("/WEB-INF/recover_password_nok.jsp").forward(request, response);
        }
    }

    protected String sendRecoverEmail(HttpServletRequest request) {
        String registerError = null;
        Connection con = null;

        String username = request.getParameter("username");

        try {
            
            javax.naming.InitialContext cxt = new javax.naming.InitialContext();
            javax.sql.DataSource ds = (javax.sql.DataSource) cxt.lookup("java:/comp/env/jdbc/DB");

            con = ds.getConnection();
            con.setAutoCommit(false);
            
            try (PreparedStatement pst = con.prepareStatement("select id from users where UPPER(username)=UPPER(?)")) {
                pst.setString(1, username);
                try (ResultSet rs = pst.executeQuery()) {
                    if( rs.next() ) {

                        long userId = rs.getLong(1);
                        
                        String uuid = java.util.UUID.randomUUID().toString();

                        try (PreparedStatement pst2 = con.prepareStatement("update users set validation_id=?, password_recover_date=NOW() where id=?")) {
                            pst2.setString(1, uuid);
                            pst2.setLong(2, userId);
                            pst2.executeUpdate();

                            //send email
                            Mail.sendRecoverEmail(Utils.getAppURL(request), username, uuid);
                        }
                        
                        
                    } else {
                        return "Email was not found.";
                    }
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
            return "An error has occurred. Try later.";
        } finally {
            if( con != null ) {
                try {
                    con.commit();
                    con.close();
                } catch(Exception e) {
                }
            }
        }
        return registerError;
    }

    protected String changePassword(HttpServletRequest request) {
        Connection con = null;

        String username = request.getParameter("email");
        String id = request.getParameter("id");

        try {
            
            javax.naming.InitialContext cxt = new javax.naming.InitialContext();
            javax.sql.DataSource ds = (javax.sql.DataSource) cxt.lookup("java:/comp/env/jdbc/DB");

            con = ds.getConnection();
            
            
            long userId = -1;
            try (PreparedStatement pst = con.prepareStatement("select id from users where UPPER(username)=UPPER(?) and validation_id=?")) {
                pst.setString(1, username);
                pst.setString(2, id);
                try (ResultSet rs = pst.executeQuery()) {
                    if( rs.next() ) {
                        userId = rs.getLong(1);
                    }
                }
            }
            
            if( userId!=-1 ) {
                try (PreparedStatement pst = con.prepareStatement("select password_recover_date from users where id=?")) {
                    pst.setLong(1, userId);
                    try (ResultSet rs = pst.executeQuery()) {
                        if( rs.next() ) {
                            Date data = rs.getTimestamp(1);
                            data = Date.from(data.toInstant().plus(Duration.ofHours(1)));
                            if( (new Date()).after(data)) {
                                return "The password recover link has more than 1 hour, so is invalid.";
                            }
                        } else {
                            return "Invalid password recover link.";
                        }
                    }
                }

                String password = request.getParameter("password");
                
                java.security.MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
                byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
                StringBuilder hexString = new StringBuilder();
                for( int i = 0; i < hash.length; i++ ) {
                    String hex = Integer.toHexString(0xff & hash[i]);
                    if( hex.length() == 1 ) {
                        hexString.append('0');
                    }
                    hexString.append(hex);
                }
                password = hexString.toString();

                try (PreparedStatement pst = con.prepareStatement("update users set verified=1, validation_id=null, password_recover_date=null, password=? where id=?")) {
                    pst.setString(1, password);
                    pst.setLong(2, userId);
                    pst.executeUpdate();
                }
                return null;
            } else {
                return "Invalid password recover link.";
            }
        } catch(Exception e) {
            e.printStackTrace();
            return "An error has occurred. Try later.";
        } finally {
            if( con != null ) {
                try {
                    con.commit();
                    con.close();
                } catch(Exception e) {
                }
            }
        }
    }
    
    protected String verifyPasswordDate(HttpServletRequest request) {
        Connection con = null;

        String username = request.getParameter("email");
        String id = request.getParameter("id");

        try {
            
            javax.naming.InitialContext cxt = new javax.naming.InitialContext();
            javax.sql.DataSource ds = (javax.sql.DataSource) cxt.lookup("java:/comp/env/jdbc/DB");

            con = ds.getConnection();
            
            
            long userId = -1;
            try (PreparedStatement pst = con.prepareStatement("select id from users where UPPER(username)=UPPER(?) and validation_id=?")) {
                pst.setString(1, username);
                pst.setString(2, id);
                try (ResultSet rs = pst.executeQuery()) {
                    if( rs.next() ) {
                        userId = rs.getLong(1);
                    }
                }
            }
            
            if( userId!=-1 ) {
                try (PreparedStatement pst = con.prepareStatement("select password_recover_date from users where id=?")) {
                    pst.setLong(1, userId);
                    try (ResultSet rs = pst.executeQuery()) {
                        if( rs.next() ) {
                            Date data = rs.getTimestamp(1);
                            data = Date.from(data.toInstant().plus(Duration.ofHours(1)));
                            if( (new Date()).after(data)) {
                                return "The password recover link has more than 1 hour, so is invalid.";
                            }
                        } else {
                            return "Invalid password recover link.";
                        }
                    }
                }

                return null;
            } else {
                return "Invalid password recover link.";
            }
        } catch(Exception e) {
            e.printStackTrace();
            return "An error has occurred. Try later.";
        } finally {
            if( con != null ) {
                try {
                    con.commit();
                    con.close();
                } catch(Exception e) {
                }
            }
        }
    }
    
}
