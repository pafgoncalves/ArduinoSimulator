<%-- 
    Document   : login.jsp
    Created on : 26/mai/2019, 20:46:30
    Author     : pafgoncalves@ipc.pt
--%>

<%@page import="java.util.Map"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    Map<String, String> messages = (Map<String, String>)request.getAttribute("messages");
    String password=null;
    String password2=null;
    
    if( messages!=null ) {
        password = messages.get("password");
        password2 = messages.get("password2");
    }
%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Arduino Simulator</title>
        <link rel="icon" href="favicon.ico" type="image/x-icon"/>
        <style>
            form td {
                padding-bottom: 5px;
                padding-left: 10px;
            }
        </style>
    </head>
    <body>
        <div class="logo" style="border-bottom: 2px solid #eee; margin-bottom: 2em;">
            <img src="arduino_logo.png" style="width: 40%; max-width: 200px; margin: auto;">
        </div>
        
        <p>Set a new password for your account</p>
        
        <form method="POST" action="recover_password">
            <input type="hidden" name="id" value="<%=request.getAttribute("id")%>">
            <input type="hidden" name="email" value="<%=request.getAttribute("email")%>">
            <table border="0">
                <tr>
                    <td>Password</td>
                    <td><input type="password" name="password"></td>
                    <td><%=password!=null?"<span style='color: red;'>"+password+"</span>":""%></td>
                </tr>
                <tr>
                    <td>Repeat password</td>
                    <td><input type="password" name="password2"></td>
                    <td><%=password2!=null?"<span style='color: red;'>"+password2+"</span>":""%></td>
                </tr>
                <tr style="height: 20px;">
                    <td colspan="3"></td>
                </tr>
                <tr>
                    <td colspan="2" align="center">
                        <input type="submit" value="Change password">
                    </td>
                    <td></td>
                </tr>
            </table>
        </form>

        <a href=".">Home</a>
                    
    </body>
</html>
