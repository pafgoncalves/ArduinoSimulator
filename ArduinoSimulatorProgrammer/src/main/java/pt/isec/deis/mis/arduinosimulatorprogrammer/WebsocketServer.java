/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.isec.deis.mis.arduinosimulatorprogrammer;

import java.io.IOException;
import java.io.StringReader;
import java.net.BindException;
import java.util.HashMap;
import java.util.Map;
import javax.json.Json;
import javax.json.JsonObject;
import javax.websocket.CloseReason;
import javax.websocket.DeploymentException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import org.glassfish.tyrus.server.Server;

/**
 *
 * @author pafgoncalves@ipc.pt
 *
 * https://github.com/tyrus-project/tyrus/blob/7f4a95d099d753ca4d4c78f781133696c896685b/samples/btc-xchange/src/test/java/org/glassfish/tyrus/sample/btc/xchange/Main.java
 * https://github.com/tyrus-project/tyrus/blob/7f4a95d099d753ca4d4c78f781133696c896685b/samples/btc-xchange/pom.xml
 */

@ServerEndpoint("/arduino")
public class WebsocketServer {
    Session userSession = null;
    private static MessageHandler messageHandler;
    Server server = null;
    
    
    public void startServer(MessageHandler msgHandler) throws IOException {
        messageHandler = msgHandler;
        
        int[] ports = new int[] { 8085, 8086, 8087 };
        final Map<String, Object> serverProperties = new HashMap<>();

        boolean started = false;
        for(int port : ports) {
            try {
                server = new Server("127.0.0.1", port, "/", serverProperties, WebsocketServer.class);

                server.start();
                started = true;
                break;
            } catch(DeploymentException e) {
                if( e.getCause() instanceof BindException ) {
                    continue;
                }
                throw new IOException(e);
            }
        }
        if( !started ) {
            throw new IOException("no port available");
        }
    }
    
    
    public void close() {
        messageHandler = null;
        if( server!=null ) {
            server.stop();
        }
    }

    /**
     * Callback hook for Connection open events.
     *
     * @param userSession the userSession which is opened.
     */
    @OnOpen
    public void onOpen(Session userSession) {
        System.out.println("client connected");
        this.userSession = userSession;
    }

    /**
     * Callback hook for Connection close events.
     *
     * @param userSession the userSession which is getting closed.
     * @param reason the reason for connection close
     */
    @OnClose
    public void onClose(Session userSession, CloseReason reason) {
//        System.out.println("closing websocket");
        this.userSession = null;
    }

    /**
     * Callback hook for Message Events. This method will be invoked when a client send a message.
     *
     * @param message The text message
     */
    @OnMessage
    public void onMessage(String message) {
        if (messageHandler != null) {
            JsonObject jsonObject = Json.createReader(new StringReader(message)).readObject();
            messageHandler.handleMessage(jsonObject);
        }
    }

    /**
     * register message handler
     *
     * @param msgHandler
     */
    public void addMessageHandler(MessageHandler msgHandler) {
        messageHandler = msgHandler;
    }

    /**
     * Send a message.
     *
     * @param json
     */
    public void sendMessage(JsonObject json) {
        if( userSession!=null ) {
            this.userSession.getAsyncRemote().sendText(json.toString());
        }
    }

    /**
     * Message handler.
     */
    public static interface MessageHandler {

        public void handleMessage(JsonObject json);
    }    
}
