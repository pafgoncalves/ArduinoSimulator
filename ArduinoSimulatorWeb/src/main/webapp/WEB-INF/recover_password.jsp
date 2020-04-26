<%-- 
    Document   : login.jsp
    Created on : 26/mai/2019, 20:46:30
    Author     : pafgoncalves@ipc.pt
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
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
        
        <form method="POST" action="recover_password">
            <table border="0">
                <tr>
                    <td>Username</td>
                    <td><input type="text" name="username"></td>
                </tr>
                <tr style="height: 20px;">
                    <td colspan="3"></td>
                </tr>
                <tr>
                    <td colspan="2" align="center">
                        <input type="submit" value="Recover">
                    </td>
                    <td></td>
                </tr>
            </table>
        </form>

        <a href=".">Home</a>
                    
    </body>
</html>
