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
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.sql.Time;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author pafgoncalves@ipc.pt
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        Map<String, String> messages = new HashMap<>();

        if( username == null || username.isEmpty() ) {
            messages.put("username", "Please enter username");
        }

        if( password == null || password.isEmpty() ) {
            messages.put("password", "Please enter password");
        }

        if( messages.isEmpty() ) {

            boolean loginOk = validateCredentials(request);

            if( loginOk ) {
                response.sendRedirect(request.getContextPath() + "/app/");
                return;
            } else {
                messages.put("login", "Unknown login, please try again");
            }
        }

        if( username != null && !username.isEmpty() ) {
            messages.put("previousUsername", username);
        }

        request.setAttribute("messages", messages);
        request.getRequestDispatcher("/WEB-INF/login.jsp").forward(request, response);
    }

    protected boolean validateCredentials(HttpServletRequest request) {
        boolean loginOk = false;
        Connection con = null;

        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String ip = request.getRemoteAddr();

        try {
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

            javax.naming.InitialContext cxt = new javax.naming.InitialContext();
            javax.sql.DataSource ds = (javax.sql.DataSource) cxt.lookup("java:/comp/env/jdbc/DB");

            con = ds.getConnection();

            try (PreparedStatement pst = con.prepareStatement("select id, verified, is_admin from users where UPPER(username)=UPPER(?) and UPPER(password)=UPPER(?)")) {
                pst.setString(1, username);
                pst.setString(2, password);
                try (ResultSet rs = pst.executeQuery()) {
                    if( rs.next() ) {

                        int verified = rs.getInt("verified");
                        if( verified!=1 ) {
                            //TODO: mostrar mensagem a dizer que a conta não está verificada
                            return false;
                        }
                        
                        request.getSession().setAttribute("user", username);
                        request.getSession().setAttribute("is_admin", rs.getInt("is_admin"));

                        loginOk = true;

                        long userId = rs.getLong("id");
                        try (PreparedStatement log = con.prepareStatement("insert into authentication_log (user_id, date_login, ip) values (?, NOW(), ?)", Statement.RETURN_GENERATED_KEYS)) {
                            log.setLong(1, userId);
                            log.setString(2, ip);
                            log.executeUpdate();

                            try (ResultSet rsKeys = log.getGeneratedKeys()) {
                                if( rsKeys.next() ) {
                                    long newId = rsKeys.getLong(1);
                                    request.getSession().setAttribute("authentication_log_id", newId);
                                }
                            }
                        }
                    }
                }
            }

        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            if( con != null ) {
                try {
                    con.close();
                } catch(Exception e) {
                }
            }
        }
        return loginOk;
    }

}
