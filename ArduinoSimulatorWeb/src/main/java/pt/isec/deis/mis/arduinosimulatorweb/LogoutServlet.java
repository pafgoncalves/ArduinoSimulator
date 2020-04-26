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
@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if( session!=null ) {
            try {
                long userId = (Long)request.getSession().getAttribute("authentication_log_id");
                registerLogout(userId, 1);
            } catch(Exception e) {}   
            session.invalidate();
        }
        response.sendRedirect(request.getContextPath() + "/");
    }


    public static void registerLogout(long authenticationLogId, int type) {
        Connection con = null;
        try {
            javax.naming.InitialContext cxt = new javax.naming.InitialContext();
            javax.sql.DataSource ds = (javax.sql.DataSource) cxt.lookup("java:/comp/env/jdbc/DB");

            con = ds.getConnection();
            
            try (PreparedStatement log = con.prepareStatement("update authentication_log set date_logout=NOW(), logout_type=? where id=? and date_logout is null")) {
                log.setInt(1, type);
                log.setLong(2, authenticationLogId);
                log.executeUpdate();
            }
            
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            if( con!=null ) {
                try {
                    con.close();
                } catch(Exception e) {}
            }
        }
    }    
}

