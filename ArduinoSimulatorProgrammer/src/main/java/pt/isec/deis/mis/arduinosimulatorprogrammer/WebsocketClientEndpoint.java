/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.isec.deis.mis.arduinosimulatorprogrammer;

import java.io.StringReader;
import java.net.URI;
import javax.json.Json;
import javax.json.JsonObject;
import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

/**
 *
 * @author pafgoncalves@ipc.pt
 */
@ClientEndpoint
public class WebsocketClientEndpoint {

    Session userSession = null;
    private MessageHandler messageHandler;
    private boolean closing = false;
    private URI endpointURI = null;

    public WebsocketClientEndpoint(URI endpointURI) {
        this(endpointURI, null);
    }
    
    public WebsocketClientEndpoint(URI endpointURI, MessageHandler msgHandler) {
        this.messageHandler = msgHandler;
        this.endpointURI = endpointURI;
    }

    public void connect() {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, endpointURI);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Callback hook for Connection open events.
     *
     * @param userSession the userSession which is opened.
     */
    @OnOpen
    public void onOpen(Session userSession) {
//        System.out.println("opening websocket");
        this.userSession = userSession;
        if( messageHandler!=null ) {
            messageHandler.connected();
        }
    }

    /**
     * Callback hook for Connection close events.
     *
     * @param userSession the userSession which is getting closed.
     * @param reason the reason for connection close
     */
    @OnClose
    public void onClose(Session userSession, CloseReason reason) {
        this.userSession = null;
        if( !closing && messageHandler!=null ) {
            messageHandler.close();
        }
    }

    /**
     * Callback hook for Message Events. This method will be invoked when a client send a message.
     *
     * @param message The text message
     */
    @OnMessage
    public void onMessage(String message) {
        if (this.messageHandler != null) {
            JsonObject jsonObject = Json.createReader(new StringReader(message)).readObject();
            this.messageHandler.handleMessage(jsonObject);
        }
    }

    /**
     * register message handler
     *
     * @param msgHandler
     */
    public void addMessageHandler(MessageHandler msgHandler) {
        this.messageHandler = msgHandler;
    }

    /**
     * Send a message.
     *
     * @param message
     */
    public void sendMessage(JsonObject message) {
        try {
            this.userSession.getBasicRemote().sendText(message.toString());
        } catch(Exception e) {
            System.out.println("Exception: "+e.getMessage());
        }
    }

    public void close() {
        closing = true;
        try {
            this.userSession.close();
        } catch(Exception e) {}
    }
    
    /**
     * Message handler.
     */
    public static interface MessageHandler {

        public void handleMessage(JsonObject message);
        public void connected();
        public void close();
    }
}

