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
    String name=null;
    String previousUsername=null;
    String previousName=null;
    String password=null;
    String password2=null;
    String register=null;
    
    if( messages!=null ) {
        username = messages.get("username");
        name = messages.get("name");
        previousUsername = messages.get("previousUsername");
        previousName = messages.get("previousName");
        password = messages.get("password");
        password2 = messages.get("password");
        register = messages.get("register");
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
        
        
        <form method="POST" action="register" enctype="application/x-www-form-urlencoded;charset=UTF-8">
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
                <tr>
                    <td>Repeat password</td>
                    <td><input type="password" name="password2"></td>
                    <td><%=password2!=null?"<span style='color: red;'>"+password2+"</span>":""%></td>
                </tr>
                <tr>
                    <td>Name</td>
                    <td><input type="text" name="name" value="<%=previousName==null?"":previousName%>"></td>
                    <td><%=name!=null?"<span style='color: red;'>"+name+"</span>":""%></td>
                </tr>
                <tr style="height: 20px;">
                    <td colspan="3"></td>
                </tr>
                <tr>
                    <td colspan="2" align="center">
                        <input type="submit" value="Register">
                    </td>
                    <td></td>
                </tr>
                <tr style="height: 20px;">
                    <td colspan="3">&nbsp;</td>
                </tr>
                <tr>
                    <td colspan="2" align="center">
                        <%=register!=null?"<span style='color: red;'>"+register+"</span>":""%>
                    </td>
                    <td></td>
                </tr>
            </table>
        </form>
                    
        <a href=".">Home</a>
                    
    </body>
</html>
