<%-- 
    Document   : login.jsp
    Created on : 26/mai/2019, 20:46:30
    Author     : pafgoncalves@ipc.pt
--%>

<%@page import="java.util.Map"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    Map<String, String> messages = (Map<String, String>)request.getAttribute("messages");
    String username=null;
    String previousUsername=null;
    String password=null;
    String login=null;
    
    if( messages!=null ) {
        username = messages.get("username");
        previousUsername = messages.get("previousUsername");
        password = messages.get("password");
        login = messages.get("login");
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
        
        <form method="POST" action="login">
            <table border="0">
                <tr>
                    <td>Username</td>
                    <td><input type="text" name="username" value="<%=previousUsername==null?"":previousUsername%>"></td>
                    <td><%=username!=null?"<span style='color: red;'>"+username+"</span>":""%></td>
                </tr>
                <tr>
                    <td>Password</td>
                    <td><input type="password" name="password"></td>
                    <td><%=password!=null?"<span style='color: red;'>"+password+"</span>":""%></td>
                </tr>
                <tr style="height: 20px;">
                    <td colspan="3"></td>
                </tr>
                <tr>
                    <td colspan="2" align="center">
                        <input type="submit" value="Login">
                    </td>
                    <td></td>
                </tr>
                <tr style="height: 20px;">
                    <td colspan="3">&nbsp;</td>
                </tr>
                <tr>
                    <td colspan="2" align="center">
                        <%=login!=null?"<span style='color: red;'>"+login+"</span>":""%>
                    </td>
                    <td></td>
                </tr>
                <tr>
                    <td colspan="2" align="center">
                        <a href="recover_password">Recover password</a>
                    </td>
                    <td></td>
                </tr>
            </table>
        </form>

        <a href=".">Home</a>
                    
    </body>
</html>
