<%@page import="java.util.Properties"%>
<%@page contentType="text/json" pageEncoding="UTF-8"
%><%!
    
    String getAppURL(HttpServletRequest request) {
        String scheme = request.getScheme();
        int port = request.getServerPort();
        String appPath = scheme + "://" + request.getServerName();
        if( 
                (!scheme.equalsIgnoreCase("http") || port!=80 )
                &&
                (!scheme.equalsIgnoreCase("https") || port!=443 )
                ) {
            appPath += ":"+port;
        }
        appPath += request.getContextPath();
        return appPath;
    }
    
    java.util.Map<String,Integer> sizes = new java.util.HashMap<>();
    
    String getSHA(String filename) {
        java.io.InputStream in = getServletContext().getResourceAsStream(filename);
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] buf = new byte[1024];
            int size = 0;
            int len;
            while( (len=in.read(buf))>0 ) {
                size += len;
                digest.update(buf,0,len);
            }
            sizes.put(filename, size);
            try {
                in.close();
            } catch(Exception e) {}
            byte[] hash = digest.digest();
            StringBuilder hexString = new StringBuilder("SHA-256:");
            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if(hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch(Exception e) {}
        return "";
    }
    
    int getSize(String filename) {
        if( sizes.containsKey(filename) ) {
            return sizes.get(filename);
        }
        java.io.InputStream in = getServletContext().getResourceAsStream(filename);
        try {
            byte[] buf = new byte[1024];
            int size = 0;
            int len;
            while( (len=in.read(buf))>0 ) {
                size += len;
            }
            sizes.put(filename, size);
            try {
                in.close();
            } catch(Exception e) {}
            return size;
        } catch(Exception e) {}
        return -1;
    }

%><%

    //TODO: salvar na base de dados o acesso ao ficheiro
    
    java.io.InputStream in = getClass().getResourceAsStream("/pt/isec/deis/mis/arduinosimulatorweb/platform.properties");
    Properties props = new Properties();
    props.load(in);
    
    String packageVersion = props.getProperty("platform.version");
    String programmerVersion = props.getProperty("programmer.version");

%>{
  "packages": [
    {
      "name": "arduinosim",
      "maintainer": "Paulo Gonçalves", 
      "help": {
        "online": "<%=getAppURL(request)+"/"%>"
      }, 
      "websiteURL": "<%=getAppURL(request)+"/"%>", 
      "email": "pafgoncalves@ipc.pt",
      "platforms": [
        {
          "category": "Contributed", 
          "help": {
            "online": "<%=getAppURL(request)+"/"%>"
          }, 
          "url": "<%=getAppURL(request)%>/package/arduinosim-<%=packageVersion%>.tar.gz", 
          "checksum": "<%=getSHA("/package/arduinosim-"+packageVersion+".tar.gz")%>", 
          "name": "arduinosim", 
          "version": "<%=packageVersion%>", 
          "architecture": "avr", 
          "archiveFileName": "arduinosim-<%=packageVersion%>.tar.gz", 
          "size": "<%=getSize("/package/arduinosim-"+packageVersion+".tar.gz")%>", 
          "toolsDependencies": [
            {
              "packager": "arduinosim", 
              "version": "<%=programmerVersion%>", 
              "name": "simupload"
            } 
          ], 
          "boards": [
            {
              "name": "Arduino Uno Simulator"
            }
          ]
        } 
      ],
    "tools": [
        {
          "version": "<%=programmerVersion%>", 
          "name": "simupload", 
          "systems": [
<% 
    String[] hosts = new String[] {
        "i386-apple-darwin",
        "i686-linux-gnu",
        "i686-mingw32",
        "x86_64-apple-darwin",
        "x86_64-linux-gnu",
        "x86_64-mingw32",
    };
    for(int i=0; i<hosts.length; i++) { 
        String host = hosts[i];
%>          
            {
              "url": "<%=getAppURL(request)%>/package/<%=host%>.tar.gz", 
              "checksum": "<%=getSHA("/package/"+host+".tar.gz")%>", 
              "host": "<%=host%>", 
              "archiveFileName": "programmer-<%=programmerVersion%>.tar.gz", 
              "size": "<%=getSize("/package/"+host+".tar.gz")%>"
            }<%=(i==hosts.length-1)?"":","%>
<%  } %>
          ]
        }
      ]
    }
  ]
}

