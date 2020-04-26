/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.isec.deis.mis.arduinosimulatorweb;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author pafgoncalves@ipc.pt
 */
@WebServlet("/validate_email")
public class VerifyEmailServlet extends HttpServlet {
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String id = request.getParameter("id");
        String username = request.getParameter("email");

        Connection con = null;

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
                try (PreparedStatement pst = con.prepareStatement("update users set verified=1, validation_id=null where id=?")) {
                    pst.setLong(1, userId);
                    pst.executeUpdate();
                }
                request.getRequestDispatcher("/WEB-INF/verify_ok.jsp").forward(request, response);
            } else {
                request.getRequestDispatcher("/WEB-INF/verify_nok.jsp").forward(request, response);
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
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>



}
