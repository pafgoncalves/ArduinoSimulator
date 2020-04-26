/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.isec.deis.mis.arduinosimulatorweb;

import java.io.IOException;
import java.io.StringReader;
import java.util.Base64;
import java.util.List;
import javax.json.Json;
import javax.json.JsonObject;
import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import pt.isec.deis.mis.dwarf.DwLineNumberInformation;

/**
 *
 * @author pafgoncalves@ipc.pt
 */
@ServerEndpoint("/ws/load")
public class LoadWSEndpoint {

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) throws IOException {
        //this.session = session;
    }

    @OnMessage
    public void onMessage(String message, Session session) {
//        System.out.println(message);

        JsonObject jsonObject = Json.createReader(new StringReader(message)).readObject();
        String cmd = jsonObject.getString("cmd");
        switch( cmd ) {
            case "load":
                doLoad(jsonObject, session);
                break;

            case "ping":
                ping(jsonObject, session);
                break;

            case "readFile":
                sourceFile(jsonObject, session);
                break;

            default:
                sendMessage(Json.createObjectBuilder()
                        .add("type", "response")
                        .add("status", "error")
                        .add("message", "unkown command '" + cmd + "'")
                        .build()
                        .toString(), session);
        }
    }

    @OnError
    public void error(Session session, Throwable t) {
        System.out.println("error on load ws: "+t.getMessage());
        t.printStackTrace();
    }
    
    @OnClose
    public void onClose(Session userSession, CloseReason reason) {
        System.out.println("closing websocket");
    }
    
    private void doLoad(JsonObject jsonObject, Session session) {
        String id = jsonObject.getString("simulationId");
        Simulation simulation = Simulation.getSimulation(id);

        if( simulation == null || !simulation.getSimulationId().equals(id) ) {
            sendMessage(Json.createObjectBuilder()
                    .add("type", "response")
                    .add("status", "error")
                    .add("message", "invalid simulation ID")
                    .build()
                    .toString(), session);
            sendMessage(Json.createObjectBuilder()
                    .add("type", "close")
                    .build()
                    .toString(), session);
            return;
        }

        try {
            Load load = new Load();
            load.setFileName(jsonObject.getString("fileName"));
            System.out.println("loading " + load.getFileName() + " in " + simulation.getSimulationId());
            load.setContent(Base64.getDecoder().decode(jsonObject.getString("content")));

            List<DwLineNumberInformation.File> sourceFiles = simulation.load(load);

            sendMessage(Json.createObjectBuilder()
                    .add("type", "response")
                    .add("status", "ok")
                    .build()
                    .toString(), session);

            if( sourceFiles.isEmpty() ) {
                sendMessage(Json.createObjectBuilder()
                        .add("type", "close")
                        .build()
                        .toString(), session);
            } else {

                DwLineNumberInformation.File f = sourceFiles.get(0);
                sendMessage(Json.createObjectBuilder()
                        .add("type", "readFile")
                        .add("path", f.getDirectory())
                        .add("file", f.getFilename())
                        .build()
                        .toString(), session);
                System.out.println("reading " + f.getFilename());

            }
        } catch(Exception e) {
            e.printStackTrace();
            sendMessage(Json.createObjectBuilder()
                    .add("type", "response")
                    .add("status", "error")
                    .add("message", e.getMessage())
                    .build()
                    .toString(), session);
            sendMessage(Json.createObjectBuilder()
                    .add("type", "close")
                    .build()
                    .toString(), session);
        }
    }

    private void ping(JsonObject jsonObject, Session session) {
        String status = "error";
        String id = jsonObject.getString("simulationId");

        Simulation simulation = Simulation.getSimulation(id);

        if( simulation != null && simulation.getSimulationId().equals(id) ) {
            status = "ok";
        }
        sendMessage(Json.createObjectBuilder()
                .add("type", "ping")
                .add("status", status)
                .build()
                .toString(), session);
    }

    private void sourceFile(JsonObject jsonObject, Session session) {
        String status = "error";
        String id = jsonObject.getString("simulationId");

        Simulation simulation = Simulation.getSimulation(id);

        if( simulation != null && simulation.getSimulationId().equals(id) ) {
            status = "ok";
        }
        sendMessage(Json.createObjectBuilder()
                .add("type", "response")
                .add("status", status)
                .build()
                .toString(), session);

        String path = jsonObject.getString("path");
        String name = jsonObject.getString("file");

        if( simulation != null && !jsonObject.containsKey("error") ) {
            simulation.addSourceFile(name,
                    new String(Base64.getDecoder().decode(jsonObject.getString("content"))));
        }

        if( simulation != null ) {
            List<DwLineNumberInformation.File> sourceFiles = simulation.getSourceFilesToDownload();
            DwLineNumberInformation.File currentFile = null;
            synchronized( sourceFiles ) {
                for( DwLineNumberInformation.File f : sourceFiles ) {
                    if( f.getDirectory().equals(path) && f.getFilename().equals(name) ) {
                        currentFile = f;
                        break;
                    }
                }
            }
            sourceFiles.remove(currentFile);
            if( sourceFiles.isEmpty() ) {
                System.out.println("closing");
                sendMessage(Json.createObjectBuilder()
                        .add("type", "close")
                        .build()
                        .toString(), session);
            } else {
                DwLineNumberInformation.File f = sourceFiles.get(0);
                sendMessage(Json.createObjectBuilder()
                        .add("type", "readFile")
                        .add("path", f.getDirectory())
                        .add("file", f.getFilename())
                        .build()
                        .toString(), session);
                System.out.println("reading " + f.getFilename());
            }
        }

    }

    public void sendMessage(String message, Session session) {
        try {
            session.getBasicRemote().sendText(message);
        } catch(Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }
    }

}
