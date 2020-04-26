<%-- 
    Document   : db
    Created on : 4/out/2019, 21:33:22
    Author     : pafgoncalves@ipc.pt
--%>

<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@page import="java.sql.ResultSetMetaData"%>
<%@page import="java.sql.ResultSet"%>
<%@page import="java.sql.Statement"%>
<%@page import="java.sql.Connection"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%!
    String query = "";
    List<String> queries;
%>
<%
    queries = (List)session.getAttribute("queries");
    if( queries==null ) {
        queries = new ArrayList();
        session.setAttribute("queries", queries);
    }
    if( request.getParameter("query") != null ) {
        query = request.getParameter("query");
        
        if( !queries.contains(query) ) {
            queries.add(query);
        }
    }
%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Database Management</title>
    </head>
    <body>
        
        <form method="POST">
            <table border="0">
                <tr>
                    <td></td>
                    <td>
                        <select onchange="document.getElementById('query').value = this.options[this.selectedIndex].text;">
<%
    for(String s : queries) {
%>
                            <option><%=s%></option>
<%
    }
%>
                        </select>
                    </td>
                </tr>
                <tr>
                    <td>Query</td>
                    <td>
                        <textarea id="query" name="query" cols="100" rows="6"><%=query%></textarea>
                    </td>
                </tr>
                <tr>
                    <td colspan="2"></td>
                </tr>
                <tr>
                    <td colspan="2" align="center">
                        <input type="submit" name="submit" value="Run">
                    </td>
                </tr>
            </table>
        </form>
                        
        <p>&nbsp;</p>
        <p>&nbsp;</p>
<%
    
    if( query != null ) {
        Connection con = null;
    
        try {
            
            javax.naming.InitialContext cxt = new javax.naming.InitialContext();
            javax.sql.DataSource ds = (javax.sql.DataSource) cxt.lookup("java:/comp/env/jdbc/DB");

            con = ds.getConnection();
            
            Statement st = con.createStatement();
            boolean result = st.execute(query);
            if( result ) {
                ResultSet rs = st.getResultSet();
                
                ResultSetMetaData rsmd = rs.getMetaData();
                out.println("<table border=1>");
                out.println("<tr>");
                for(int i=1; i<=rsmd.getColumnCount(); i++) {
                    out.println("<th>"+rsmd.getColumnLabel(i)+"</th>");
                }
                out.println("</tr>");
                while( rs.next() ) {
                    out.println("<tr>");
                    for(int i=1; i<=rsmd.getColumnCount(); i++) {
                        Object obj = rs.getObject(i);
                        out.println("<th>"+(obj==null?"null":obj.toString())+"</th>");
                    }
                    out.println("</tr>");
                }
                out.println("</table>");
            } else {
                out.println("row count update: "+st.getUpdateCount());
            }
            
            con.commit(); 
        } catch(Exception e) {
            //e.printStackTrace();
            out.println("<pre style='color: red;'>"+e.getMessage()+"</pre>");
        } finally {
            if( con != null ) {
                try {
                    con.close();
                } catch(Exception e) {}
            }
        }    
    }
%>
    </body>
</html>
