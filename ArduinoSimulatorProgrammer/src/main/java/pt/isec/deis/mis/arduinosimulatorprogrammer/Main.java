/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.isec.deis.mis.arduinosimulatorprogrammer;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

/**
 *
 * @author pafgoncalves@ipc.pt
 */
public class Main {

    private final int PING_TIMEOUT      = 5_000;
    private final int CLIENT_TIMEOUT    = 10_000;
    
    private File file = null;
    private WebsocketClientEndpoint clientEndPoint = null;
    private SyncronizeObj pingSync = new SyncronizeObj();
    private String pingResponse = null;
    private String fileContent = null;
    private String simulationId = null;
    private boolean exitOnClose = false;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        Logger.getLogger("").setLevel(Level.OFF);

        String fileName = null;

        for( String p : args ) {
            if( p.startsWith("-Uflash:w:") ) {
                String[] tmp = p.split(":", 3);
                fileName = tmp[2];
                if( fileName.endsWith(":i") ) {
                    fileName = fileName.substring(0, fileName.length() - 2);
                }
            }
        }

        Main main = new Main(fileName);
    }

    public Main(String filename) {
        try {

            if( filename == null ) {
                System.err.println("Missing parameter \"-Uflash:w:<filepath>:i\"");
                return;
            }

            filename = filename.replace(".ino.hex", ".ino.elf");
            
            file = new File(filename);
            if( !file.exists() ) {
                System.err.println("File " + file.getAbsolutePath() + " does not exists");
                return;
            }

            System.out.println("file: " + file.getName());

            loadContent();

            final Preferences pref = Preferences.userNodeForPackage(Main.class);
            String address = pref.get("address", null);
            String id = pref.get("simulationId", null);

            boolean simulationIdIsValid = false;

            if( address != null && id != null ) {
                System.out.println("previous data found: " + id + " " + address);

                connect(address, id);

                //wait for response
                while( pingResponse == null ) {
                    pingSync.doWait(PING_TIMEOUT);
                }

                if( "ok".equals(pingResponse) ) {
                    System.out.println("previous data valid");
                    simulationIdIsValid = true;
                    // send message to websocket
                    //sendLoad(id, content);
                } else {
                    System.out.println("previous data not valid");
                    if( clientEndPoint != null ) {
                        clientEndPoint.close();
                        clientEndPoint = null;
                    }
                }
            }

            if( !simulationIdIsValid ) {
                System.out.println("Waiting for client to connect...");

                try {
                    pingResponse = null;
                    WebsocketServer wsserver = new WebsocketServer();
                    wsserver.startServer(new WebsocketServer.MessageHandler() {
                        @Override
                        public void handleMessage(JsonObject json) {
                            String cmd = json.getString("cmd");
                            switch( cmd ) {
                                case "webClient":
                                    String address = json.getString("address");
                                    String id = json.getString("simulationId");
                                    System.out.println("simulationId: " + id);
                                    System.out.println("server address: " + address);
                                    pref.put("address", address);
                                    pref.put("simulationId", id);
                                    try {
                                        pref.sync();
                                    } catch(Exception e) {
                                    }
                                    connect(address, id);
                                    break;
                            }
                        }
                    });

                    //wait for response
                    if( pingResponse == null ) {
                        pingSync.doWait(CLIENT_TIMEOUT);
                    }
                    wsserver.close();
                    if( "ok".equals(pingResponse) ) {
                        System.out.println("data valid");
                        simulationIdIsValid = true;
                    } else {
                        System.out.println("No client has connected");
                    }
                } catch(IOException ioe) {
                    System.err.println(ioe.getMessage());
                }
            }

            if( !simulationIdIsValid ) {
                System.out.println("Asking user for the data...");

                //ask for address to the user
                String title = "Enter server programming URL & Simulation ID";
                JTextField[] tf = new JTextField[2];
                tf[0] = new JTextField();
                tf[1] = new JTextField();
                boolean pressedOk = JOptionPane.showConfirmDialog(null, tf, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION;
                String url = tf[0].getText();
                String userId = tf[1].getText();
                if( pressedOk && url != null && !url.trim().isEmpty() ) {
                    pref.put("address", url);
                    pref.put("simulationId", userId);
                    try {
                        pref.sync();
                    } catch(Exception e) {
                    }
                    //sendLoad(userId, content);
                    pingResponse = null;
                    connect(url, id);
                    //wait for response
                    while( pingResponse == null ) {
                        pingSync.doWait(PING_TIMEOUT);
                    }

                    if( "ok".equals(pingResponse) ) {
                        System.out.println("data valid");
                        simulationIdIsValid = true;
                    } else {
                        System.out.println("Invalid data supplied by user");
                    }
                } else {
                    System.out.println("User canceled");
                }
            }

            if( simulationIdIsValid ) {
                sendLoad();
                Thread.currentThread().join();
            }
        } catch(Exception ex) {
            ex.printStackTrace();
            System.err.println("Exception: " + ex.getMessage());
            System.exit(1);
        }
    }


    private void connect(String address, final String id) {
        simulationId = id;
        try {
            clientEndPoint = new WebsocketClientEndpoint(new URI(address), new WebsocketClientEndpoint.MessageHandler() {
                @Override
                public void handleMessage(JsonObject message) {
                    String type = message.getString("type");
                    switch( type ) {
                        case "ping":
                            pingResponse = message.getString("status");
                            if( pingResponse==null ) {
                                pingResponse = "fail";
                            }   
                            pingSync.doNotify();
                            break;
                        case "readFile":
                            readFile(message.getString("path"), message.getString("file"));
                            break;
                        case "response":
                            String status = message.getString("status");
                            System.out.print(status);
                            if( !"ok".equals(status) ) {
                                System.out.print(": "+message.getString("message"));
                            }   
                            System.out.println();
                            break;
                        case "close":
                            clientEndPoint.close();
                            System.exit(0);
                        default:
                            System.out.println("unknown command: "+type);
                            break;
                    }
                }

                @Override
                public void close() {
                    if( exitOnClose ) {
                        System.out.println("Unexpected close");
                        System.exit(3);
                    }
                    //se fechou devido a erro não vale a pena ficar à espera
                    pingResponse = "fail";
                    pingSync.doNotify();
                }

                @Override
                public void connected() {
                    ping(id);
                }
            });
            clientEndPoint.connect();
        } catch(Exception ex) {
            ex.printStackTrace();
            System.err.println("Exception: " + ex.getMessage());
            //se fechou devido a erro não vale a pena ficar à espera
            pingResponse = "fail";
            pingSync.doNotify();
        }
    }


    private void ping(String id) {

        try {
            // send message to websocket
            JsonObjectBuilder builder = Json.createObjectBuilder()
                    .add("cmd", "ping")
                    .add("simulationId", id == null ? "0000" : id);
            clientEndPoint.sendMessage(builder.build());

        } catch(Exception ex) {
            ex.printStackTrace();
            System.err.println("Exception: " + ex.getMessage());
            //se deu erro não vale a pena ficar à espera
            pingResponse = "fail";
            pingSync.doNotify();
        }
    }
    
    private void loadContent() {
        try {
            FileInputStream in = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] tmp = new byte[1024];
            int tam;
            while( (tam=in.read(tmp))>0 ) {
                bos.write(tmp, 0, tam);
            }
            fileContent = Base64.getEncoder().encodeToString(bos.toByteArray());
        } catch(Exception ex) {
            ex.printStackTrace();
            System.err.println("Exception: " + ex.getMessage());
            System.exit(1);
        }
    }

    private void readFile(String path, String filename) {
        try {
            File f = new File(path,filename);
            FileInputStream in = new FileInputStream(f);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] tmp = new byte[1024];
            int tam;
            while( (tam=in.read(tmp))>0 ) {
                bos.write(tmp, 0, tam);
            }
            String content = Base64.getEncoder().encodeToString(bos.toByteArray());
            JsonObjectBuilder builder = Json.createObjectBuilder()
                    .add("cmd", "readFile")
                    .add("simulationId", simulationId == null ? "0000" : simulationId)
                    .add("path", path)
                    .add("file", filename)
                    .add("content", content);
            clientEndPoint.sendMessage(builder.build());
        } catch(Exception ex) {
            JsonObjectBuilder builder = Json.createObjectBuilder()
                    .add("cmd", "readFile")
                    .add("simulationId", simulationId == null ? "0000" : simulationId)
                    .add("path", path)
                    .add("file", filename)
                    .add("error", "error");
            clientEndPoint.sendMessage(builder.build());
        }
    }
    
    private void sendLoad() {
        exitOnClose = true;
        try {
            // send message to websocket
            JsonObjectBuilder builder = Json.createObjectBuilder()
                    .add("cmd", "load")
                    .add("fileName", file.getName())
                    .add("simulationId", simulationId == null ? "0000" : simulationId)
                    .add("content", fileContent);
            System.out.println("content size: " + fileContent.length());
            clientEndPoint.sendMessage(builder.build());

        } catch(Exception ex) {
            ex.printStackTrace();
            System.err.println("Exception: " + ex.getMessage());
            System.exit(2);
        }

    }

}
