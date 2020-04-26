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
import java.sql.Statement;
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
@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String password2 = request.getParameter("password2");
        String name = request.getParameter("name");
        Map<String, String> messages = new HashMap<>();

        if( username == null || username.isEmpty() ) {
            messages.put("username", "Please enter username");
        } else if( !username.matches(".+@.+") ) {
            messages.put("username", "Username must be an email address");
        }

        if( password == null || password.isEmpty() ) {
            messages.put("password", "Please enter password");
        }

        if( password2 == null || password2.isEmpty() ) {
            messages.put("password2", "Please enter password");
        }

        if( password != null && !password.equals(password2) ) {
            messages.put("password2", "Passwords don't match");
        }

        if( name == null || name.isEmpty() ) {
            messages.put("name", "Please enter your name");
        }

        if( messages.isEmpty() ) {
            String registerError = registerUser(request);

            if( registerError==null ) {
                request.getRequestDispatcher("/WEB-INF/register_ok.jsp").forward(request, response);
                return;
            } else {
                messages.put("register", registerError);
            }
        }

        if( username != null && !username.isEmpty() ) {
            messages.put("previousUsername", username);
        }
        if( name != null && !name.isEmpty() ) {
            messages.put("previousName", name);
        }

        request.setAttribute("messages", messages);
        request.getRequestDispatcher("/WEB-INF/register.jsp").forward(request, response);
    }

    protected String registerUser(HttpServletRequest request) {
        String registerError = null;
        Connection con = null;

        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String name = request.getParameter("name");
        String ip = request.getRemoteAddr();

        try {
            
            javax.naming.InitialContext cxt = new javax.naming.InitialContext();
            javax.sql.DataSource ds = (javax.sql.DataSource) cxt.lookup("java:/comp/env/jdbc/DB");

            con = ds.getConnection();
            con.setAutoCommit(false);
            
            try (PreparedStatement pst = con.prepareStatement("select id, verified from users where UPPER(username)=UPPER(?)")) {
                pst.setString(1, username);
                try (ResultSet rs = pst.executeQuery()) {
                    if( rs.next() ) {
                        return "Username already exists";
                    }
                }
            }
            
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

            String uuid = java.util.UUID.randomUUID().toString();
            
            try (PreparedStatement pst = con.prepareStatement("insert into users (username, password, verified, date_created, name, validation_id) values (?, UPPER(?), 0, NOW(), ?, ?)")) {
                pst.setString(1, username);
                pst.setString(2, password);
                pst.setString(3, name);
                pst.setString(4, uuid);
                pst.executeUpdate();
                
                //send email
                Mail.sendRegisterEmail(Utils.getAppURL(request), username, uuid);
            }

            con.commit(); 
        } catch(Exception e) {
            e.printStackTrace();
            return "An error has occurred. Try later.";
        } finally {
            if( con != null ) {
                try {
                    con.close();
                } catch(Exception e) {
                }
            }
        }
        return registerError;
    }

}
