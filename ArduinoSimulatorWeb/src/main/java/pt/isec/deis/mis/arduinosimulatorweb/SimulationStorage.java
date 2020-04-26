/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.isec.deis.mis.arduinosimulatorweb;

import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import pt.isec.deis.mis.dwarf.DwLineNumberInformation;

/**
 *
 * @author pafgoncalves@ipc.pt
 */
public class SimulationStorage {

    public static void saveSimulation(Simulation sim) {
        Connection con = null;
        String username = sim.getUsername();
        try {
            javax.naming.InitialContext cxt = new javax.naming.InitialContext();
            javax.sql.DataSource ds = (javax.sql.DataSource) cxt.lookup("java:/comp/env/jdbc/DB");

            con = ds.getConnection();

            long userId = -1;
            try (PreparedStatement pst = con.prepareStatement("select id from users where username=?")) {
                pst.setString(1, username);
                try (ResultSet rs = pst.executeQuery()) {
                    if( rs.next() ) {
                        userId = rs.getLong("id");
                    }
                }
            }
            
            if( userId==-1 ) {
                return;
            }
            
            long simId = -1;
            try (PreparedStatement pst = con.prepareStatement("select id from simulation where uuid=?")) {
                pst.setString(1, sim.getSimulationId());
                try (ResultSet rs = pst.executeQuery()) {
                    if( rs.next() ) {
                        simId = rs.getLong("id");
                    }
                }
            }
            
            con.setAutoCommit(false);
            if( simId!=-1 ) {

                try (PreparedStatement update = con.prepareStatement("update simulation set "
                        + "sketchname=?, binary=?, debug_info=?, circuit=?, date_updated=NOW() "
                        + "where id=?")) {
                    update.setString(1, sim.getSketchName());
                    ByteBufferBackedInputStream bin = new ByteBufferBackedInputStream(sim.getLoad());
                    update.setBlob(2, bin);
                    
                    JsonObjectBuilder builder = Json.createObjectBuilder();
                    try {
                        JsonArrayBuilder ab = Json.createArrayBuilder();
                        for(DwLineNumberInformation.Line line : sim.getSourceLinesMapping() ) {
                            String hex = Integer.toHexString(line.getAddress()).toUpperCase();
                            while(hex.length()<4) {
                                hex = "0"+hex;
                            }
                            JsonObjectBuilder jsonLine = Json.createObjectBuilder()
                                .add("address", hex)
                                .add("line", line.getSourceLine())
                                .add("file", line.getFile().getFilename());
                            ab.add(jsonLine);
                        }
                        builder.add("sourceLinesMapping", ab);
                    } catch(Exception e) {
                    }
                    String json = builder.build().toString();
                    Clob debugClob = con.createClob();
                    debugClob.setString(1, json);
                    update.setClob(3, debugClob);
                    
                    Clob circuitClob = con.createClob();
                    circuitClob.setString(1, sim.getCircuit());
                    update.setClob(4, circuitClob);
                    update.setLong(5, simId);
                    
                    update.executeUpdate();
                }

                //apagar os ficheiros da simulação
                try (PreparedStatement delete = con.prepareStatement("delete from source_files  "
                        + "where simulation_id=?")) {
                    delete.setLong(1, simId);
                    delete.executeUpdate();
                }
                

            } else {
                try (PreparedStatement insert = con.prepareStatement("insert into simulation "
                        + "(user_id, uuid, name, sketchname, binary, debug_info, circuit, eeprom, date_created, date_updated) "
                        + "values "
                        + "(?, ?, ?, ?, ?, ?, ?, null, NOW(), NOW())", Statement.RETURN_GENERATED_KEYS)) {
                    insert.setLong(1, userId);
                    insert.setString(2, sim.getSimulationId());
                    insert.setString(3, sim.getName());
                    insert.setString(4, sim.getSketchName());
                    ByteBufferBackedInputStream bin = new ByteBufferBackedInputStream(sim.getLoad());
                    insert.setBlob(5, bin);
                    
                    JsonObjectBuilder builder = Json.createObjectBuilder();
                    try {
                        JsonArrayBuilder ab = Json.createArrayBuilder();
                        for(DwLineNumberInformation.Line line : sim.getSourceLinesMapping() ) {
                            String hex = Integer.toHexString(line.getAddress()/2).toUpperCase();
                            while(hex.length()<4) {
                                hex = "0"+hex;
                            }
                            JsonObjectBuilder jsonLine = Json.createObjectBuilder()
                                .add("address", hex)
                                .add("line", line.getSourceLine())
                                .add("file", line.getFile().getFilename());
                            ab.add(jsonLine);
                        }
                        builder.add("sourceLinesMapping", ab);
                    } catch(Exception e) {
                    }
                    String json = builder.build().toString();
                    Clob debugClob = con.createClob();
                    debugClob.setString(1, json);
                    insert.setClob(6, debugClob);
                    
                    Clob circuitClob = con.createClob();
                    circuitClob.setString(1, sim.getCircuit());
                    insert.setClob(7, circuitClob);
                    insert.executeUpdate();
                    
                    try (ResultSet rsKeys = insert.getGeneratedKeys()) {
                        if( rsKeys.next() ) {
                            simId = rsKeys.getLong(1);
                        }
                    }
                    
                }
            }
            
            //os ficheiros
            try (PreparedStatement insertFile = con.prepareStatement("insert into source_files "
                    + "(simulation_id, filename, dirname, source) "
                    + "values "
                    + "(?, ?, ?, ?)")) {

                insertFile.setLong(1, simId);
                for(DwLineNumberInformation.File f : sim.getSourceFiles()) {
                    String source = sim.getSourceFilesSource(f.getFilename());
                    if( source == null ) {
                        source = "";
                    }
                    insertFile.setString(2, f.getFilename());
                    insertFile.setString(3, f.getDirectory());
                    Clob sourceClob = con.createClob();
                    sourceClob.setString(1, source);
                    insertFile.setClob(4, sourceClob);

                    insertFile.executeUpdate();
                }
            }

            //actualizar utilizador
            try (PreparedStatement updateUser = con.prepareStatement("update users set last_simulation_id=? where id=?")) {
                updateUser.setLong(1, simId);
                updateUser.setLong(2, userId);
                updateUser.executeUpdate();
            }                
            
            con.commit();

            sim.setUserSimulations(getSimulations(username));
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            if( con != null ) {
                try {
                    con.close();
                } catch(Exception e) {
                }
            }
        }
    }

    public static void deleteSimulation(Simulation sim) {
        Connection con = null;
        String username = sim.getUsername();
        try {
            javax.naming.InitialContext cxt = new javax.naming.InitialContext();
            javax.sql.DataSource ds = (javax.sql.DataSource) cxt.lookup("java:/comp/env/jdbc/DB");

            con = ds.getConnection();

            long userId = -1;
            try (PreparedStatement pst = con.prepareStatement("select id from users where username=?")) {
                pst.setString(1, username);
                try (ResultSet rs = pst.executeQuery()) {
                    if( rs.next() ) {
                        userId = rs.getLong("id");
                    }
                }
            }
            
            if( userId==-1 ) {
                return;
            }
            
            long simId = -1;
            try (PreparedStatement pst = con.prepareStatement("select id from simulation where uuid=?")) {
                pst.setString(1, sim.getSimulationId());
                try (ResultSet rs = pst.executeQuery()) {
                    if( rs.next() ) {
                        simId = rs.getLong("id");
                    }
                }
            }
            
            con.setAutoCommit(false);
            if( simId!=-1 ) {

                //apagar os ficheiros da simulação
                try (PreparedStatement delete = con.prepareStatement("delete from source_files  "
                        + "where simulation_id=?")) {
                    delete.setLong(1, simId);
                    delete.executeUpdate();
                }
                
                //actualizar utilizador
                try (PreparedStatement updateUser = con.prepareStatement("update users set last_simulation_id=null where id=?")) {
                    updateUser.setLong(1, userId);
                    updateUser.executeUpdate();
                }                
                
                try (PreparedStatement delete = con.prepareStatement("delete from simulation where id=?")) {
                    delete.setLong(1, simId);
                    delete.executeUpdate();
                }
            }
            
            con.commit();

            sim.setUserSimulations(getSimulations(username));
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            if( con != null ) {
                try {
                    con.close();
                } catch(Exception e) {
                }
            }
        }
    }

    public static Simulation getLastSimulation(String username) {
        Connection con = null;
        try {
            javax.naming.InitialContext cxt = new javax.naming.InitialContext();
            javax.sql.DataSource ds = (javax.sql.DataSource) cxt.lookup("java:/comp/env/jdbc/DB");

            con = ds.getConnection();

            long simId = -1;
            try (PreparedStatement pst = con.prepareStatement("select last_simulation_id from users where username=?")) {
                pst.setString(1, username);
                try (ResultSet rs = pst.executeQuery()) {
                    if( rs.next() ) {
                        simId = rs.getLong("last_simulation_id");
                    }
                }
            }
            
            if( simId!=-1 ) {
                return getSimulation(simId);
            }
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            if( con != null ) {
                try {
                    con.close();
                } catch(Exception e) {
                }
            }
        }
        return null;
    }
    
    public static Simulation getSimulation(long simId) {
        Connection con = null;
        try {
            javax.naming.InitialContext cxt = new javax.naming.InitialContext();
            javax.sql.DataSource ds = (javax.sql.DataSource) cxt.lookup("java:/comp/env/jdbc/DB");

            con = ds.getConnection();

            try (PreparedStatement pst = con.prepareStatement("select * from simulation where id=?")) {
                pst.setLong(1, simId);
                try (ResultSet rs = pst.executeQuery()) {
                    if( rs.next() ) {

                        long userId = rs.getLong("user_id");
                        String simulationId = rs.getString("uuid");
                        String sketchName = rs.getString("sketchname");
                        Blob binary = rs.getBlob("binary");
                        ByteBuffer buffer = ByteBuffer.wrap(binary.getBytes(1, (int)binary.length()));
                        buffer.order(ByteOrder.LITTLE_ENDIAN);

                        List<DwLineNumberInformation.File> sourceFiles = new ArrayList<>();
                        Map<String, String> sourceFilesSource = new HashMap<>();
                        List<DwLineNumberInformation.Line> sourceFilesLines = new ArrayList<>();

                        try (PreparedStatement pstFiles = con.prepareStatement("select * from source_files where simulation_id=?")) {
                            pstFiles.setLong(1, simId);
                            try (ResultSet rsFiles = pstFiles.executeQuery()) {
                                while( rsFiles.next() ) {
                                    DwLineNumberInformation.File file = new DwLineNumberInformation.File(
                                            rsFiles.getString("filename"),
                                            rsFiles.getString("dirname"),
                                            0, 0
                                    );
                                    sourceFiles.add(file);
                                    Clob clob = rsFiles.getClob("source");
                                    sourceFilesSource.put(file.getFilename(), clob.getSubString(1, (int)clob.length()));
                                }
                            }
                        }

                        //debug_info
                        Clob clob = rs.getClob("debug_info");
                        if( clob!=null && clob.length()>0 ) {
                            String json = clob.getSubString(1, (int)clob.length());
                            
                            JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
                            JsonArray list = jsonObject.getJsonArray("sourceLinesMapping");
                            for(int i=0; i<list.size(); i++) {
                                JsonObject item = list.getJsonObject(i);
                                int address = Integer.parseInt(item.getString("address"), 16);
                                int line = item.getInt("line");
                                //TODO: reutilizar o objecto file da lista sourceFiles
                                String filename = item.getString("file");
                                DwLineNumberInformation.File file = new DwLineNumberInformation.File(filename, "", 0, 0);
                                sourceFilesLines.add(new DwLineNumberInformation.Line(address, file, line));
                            }
                        }

                        Simulation simulation = new Simulation(
                                simulationId, sketchName, 
                                sourceFiles, sourceFilesSource, 
                                sourceFilesLines, buffer);

                        simulation.setName(rs.getString("name"));
                        Clob circuit = rs.getClob("circuit");
                        if( circuit!=null && circuit.length()>0 ) {
                            simulation.setCircuit(circuit.getSubString(1, (int)circuit.length()));
                        }

                        try (PreparedStatement pstUpdateLast = con.prepareStatement("update users set last_simulation_id=? where id=?")) {
                            pstUpdateLast.setLong(1, simId);
                            pstUpdateLast.setLong(2, userId);
                            pstUpdateLast.executeUpdate();
                        }
                        
                        try (PreparedStatement pstUsername = con.prepareStatement("select username from users where id=?")) {
                            pstUsername.setLong(1, userId);
                            try (ResultSet rsUsername = pstUsername.executeQuery()) {
                                if( rsUsername.next() ) {
                                    simulation.setUsername(rsUsername.getString("username"));
                                }
                            }
                        }

                        simulation.setUserSimulations(getSimulations(simulation.getUsername()));
                        
                        return simulation;
                    }
                }
            }

        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            if( con != null ) {
                try {
                    con.close();
                } catch(Exception e) {
                }
            }
        }

        return null;
    }

    public static Map<Long,String> getSimulations(String username) {
        Connection con = null;
        Map<Long,String> simulations = new HashMap<>();
        try {
            javax.naming.InitialContext cxt = new javax.naming.InitialContext();
            javax.sql.DataSource ds = (javax.sql.DataSource) cxt.lookup("java:/comp/env/jdbc/DB");

            con = ds.getConnection();

            try (PreparedStatement pst = con.prepareStatement("select s.id, s.name "
                    + "from simulation s, users u "
                    + "where s.user_id = u.id "
                    + "and u.username=?")) {
                pst.setString(1, username);
                try (ResultSet rs = pst.executeQuery()) {
                    while( rs.next() ) {
                        simulations.put(rs.getLong("id"),rs.getString("name"));
                    }
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            if( con != null ) {
                try {
                    con.close();
                } catch(Exception e) {
                }
            }
        }

        return simulations;
    }

    public static String getCircuit(String uuid) {
        Connection con = null;
        try {
            javax.naming.InitialContext cxt = new javax.naming.InitialContext();
            javax.sql.DataSource ds = (javax.sql.DataSource) cxt.lookup("java:/comp/env/jdbc/DB");

            con = ds.getConnection();

            try (PreparedStatement pst = con.prepareStatement("select circuit from simulation where uuid=?")) {
                pst.setString(1, uuid);
                try (ResultSet rs = pst.executeQuery()) {
                    if( rs.next() ) {
                        String circuit = rs.getString("circuit");
                        return (circuit!=null && !circuit.isEmpty()) ? circuit : null;
                    }
                }
            }

        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            if( con != null ) {
                try {
                    con.close();
                } catch(Exception e) {
                }
            }
        }

        return null;
    }

}
