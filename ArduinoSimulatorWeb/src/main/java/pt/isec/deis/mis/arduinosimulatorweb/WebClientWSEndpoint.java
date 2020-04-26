/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.isec.deis.mis.arduinosimulatorweb;

import java.io.IOException;
import java.io.StringReader;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.json.Json;
import javax.json.JsonObject;
import javax.servlet.http.HttpSession;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import static pt.isec.deis.mis.arduinosimulatorweb.SimulationStorage.getSimulations;

/**
 *
 * @author pafgoncalves@ipc.pt
 * http://www.programmingforliving.com/2013/08/jsr-356-java-api-for-websocket-client-api.html
 *
 * Sessão HTTP
 * https://stackoverflow.com/questions/17936440/accessing-httpsession-from-httpservletrequest-in-a-web-socket-serverendpoint
 */
@ServerEndpoint(value = "/ws/client", configurator = GetHttpSessionConfigurator.class)
public class WebClientWSEndpoint {

    //fazer uma mapa entre Session e Simulation
    private static final Map<Session, Simulation> map = Collections.synchronizedMap(new HashMap<Session, Simulation>());
    private static final Map<Session, HttpSession> sessionMap = Collections.synchronizedMap(new HashMap<Session, HttpSession>());

    
    @OnOpen
    public void onOpen(Session session, EndpointConfig config) throws IOException {
        HttpSession httpSession = (HttpSession) config.getUserProperties()
                .get(HttpSession.class.getName());

        String username = (String)httpSession.getAttribute("user");
        Simulation simulation;
        synchronized( httpSession ) {
            simulation = (Simulation) httpSession.getAttribute("SIMULATION");
            if( simulation == null ) {
                simulation = SimulationStorage.getLastSimulation(username);
                if( simulation==null ) {
                    simulation = new Simulation();
                    simulation.setUsername(username);
                    simulation.setUserSimulations(getSimulations(username));
                }
                httpSession.setAttribute("SIMULATION", simulation);
            }
        }
        sessionMap.put(session, httpSession);
        map.put(session, simulation);
        simulation.setSession(session);
    }

    @OnClose
    public void onClose(Session session) {
        sessionMap.remove(session);
        Simulation simulation = map.remove(session);
        if( simulation!=null ) {
            simulation.setSession(null);
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        Simulation simulation = map.get(session);
        simulation.setLastContact();

        JsonObject jsonObject = Json.createReader(new StringReader(message)).readObject();
        String cmd = jsonObject.getString("cmd");

        switch( cmd ) {
            case "ping":
                simulation.ping();
                break;
            case "resume":
                simulation.resume();
                break;
            case "start":
                simulation.start();
                break;
            case "stop":
                simulation.stop();
                break;
            case "status":
                simulation.status();
                break;
            case "speed":
                simulation.speed();
                break;
            case "step":
                simulation.step();
                break;
            case "asm":
                simulation.getASM();
                simulation.getDataMemory(true);
                simulation.sendSourceFilesSource();
                simulation.sendCircuit();
                simulation.sendUserSimulations();
                break;
            case "dataMemory":
                simulation.getDataMemory();
                break;
            case "breakpoint":
                int address = -1;
                try {
                    address = Integer.parseInt(jsonObject.getString("address"), 16);
                } catch(Exception e) {
                }
                simulation.toogleBreakpoint(address);
                break;
            case "usart":
                simulation.sendSerial(jsonObject.getString("value"));
                break;
            case "pinChange":
                int pin = -1;
                Float value = null;
                try {
                    pin = jsonObject.getInt("pin");
                } catch(Exception e) {
                    e.printStackTrace();
                }
                try {
                    String str = jsonObject.get("value").toString();
//                    if( str!=null && !str.isEmpty() && !str.equals("null") ) {
                        value = Float.parseFloat(str);
//                    }
                } catch(Exception e) {
                }
                simulation.pinChange(pin, value);
                break;
            case "load":
                try {
                    Load load = new Load();
                    load.setFileName(jsonObject.getString("fileName"));
                    load.setContent(Base64.getDecoder().decode(jsonObject.getString("content")));
                    simulation.load(load);
                } catch(Exception e) {
                    simulation.exception(e);
                }
                break;
            case "save":
                simulation.setName(jsonObject.getString("name"));
                simulation.setCircuit(jsonObject.getString("circuit"));
                SimulationStorage.saveSimulation(simulation);
                simulation.sendUserSimulations();
                break;
            case "open":
                int simId = -1;
                try {
                    simId = jsonObject.getInt("id");
                } catch(Exception e) {
                    e.printStackTrace();
                }
                //TODO: validar que a simulação pertence ao utilizador actual
                simulation = SimulationStorage.getSimulation(simId);
                if( simulation!=null ) {
                    registerSimulation(session, simulation);
                    simulation.status();
                    simulation.getASM();
                    simulation.getDataMemory(true);
                    simulation.sendSourceFilesSource();
                    simulation.sendCircuit();
                } else {
                    //TODO
                    System.out.println("não encontrou "+simId);
                }
                break;
            case "delete":
                SimulationStorage.deleteSimulation(simulation);
                //sem break, pois ao apagar cria de seguida uma nova simulação
            case "new":
                String username = simulation.getUsername();
                simulation = new Simulation();
                registerSimulation(session, simulation);
                simulation.setUsername(username);
                simulation.setUserSimulations(getSimulations(username));
                
                simulation.status();
                simulation.getASM();
                simulation.getDataMemory(true);
                simulation.sendSourceFilesSource();
                simulation.sendCircuit();
                simulation.sendUserSimulations();
                
                break;
        }
    }

    @OnError
    public void error(Session session, Throwable t) {
//        System.out.println("error on ws: "+t.getMessage());
//        t.printStackTrace();
    }

    protected void registerSimulation(Session session, Simulation simulation) {
        sessionMap.get(session).removeAttribute("SIMULATION");
        map.put(session, simulation);
        simulation.setSession(session);
        sessionMap.get(session).setAttribute("SIMULATION", simulation);
    }
    
    protected void sendMessage(Session session, String message) {
        if( session != null && session.isOpen() ) {
            try {
                session.getBasicRemote().sendText(message);
            } catch(Exception e) {
                e.printStackTrace(System.err);
            }
        } else {
            System.out.println("websocket closed!!");
        }
    }
    
}
