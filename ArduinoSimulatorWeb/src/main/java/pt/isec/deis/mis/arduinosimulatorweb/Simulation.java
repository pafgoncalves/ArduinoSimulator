/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.isec.deis.mis.arduinosimulatorweb;

import com.bicirikdwarf.dwarf.CompilationUnit;
import com.bicirikdwarf.dwarf.Dwarf32Context;
import com.bicirikdwarf.elf.Elf32Context;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import javax.websocket.Session;
import pt.isec.deis.mis.arduinosimulator.ATmega328P;
import pt.isec.deis.mis.arduinosimulator.BreakpointException;
import pt.isec.deis.mis.arduinosimulator.CPU;
import pt.isec.deis.mis.arduinosimulator.ProgramMemoryLoader;
import pt.isec.deis.mis.arduinosimulator.peripherals.Usart;
import pt.isec.deis.mis.dwarf.DwLineNumberInformation;

/**
 *
 * @author pafgoncalves@ipc.pt
 */
public class Simulation implements HttpSessionBindingListener {

    protected static Map<String, Simulation> map = new HashMap<>();

    
    /**
     * Username of the user
     */
    private String username = null;
    /**
     * The name of the loaded Arduino Sketch
     */
    private String sketchName = null;
    /**
     * The name the user gives the circuit
     */
    private String name = null;
    /**
     * The circuit JSON
     */
    private String circuit = null;
    /**
     * The sketch binary
     */
    private ByteBuffer loadBinary = null;
    /**
     * List of simulations that belong to the user
     */
    private Map<Long,String> simulationsList = null;
    
    private String simulationId;
    private Session session;
    private CPU cpu;
    private Future task = null;

    private int[] previousMemory;

    private long lastContact = 0;

    private final List<DwLineNumberInformation.File> sourceFiles = new ArrayList<>();
    private final List<DwLineNumberInformation.File> sourceFilesCopy = new ArrayList<>();
    private final Map<String, String> sourceFilesSource = new HashMap<>();
    private final List<DwLineNumberInformation.Line> sourceFilesLines = new ArrayList<>();
    
    public Simulation() {
        generateSimulationId();
        init();
    }

    public Simulation(String simulationId,
            String sketchName,
            List<DwLineNumberInformation.File> sourceFiles,
            Map<String, String> sourceFilesSource,
            List<DwLineNumberInformation.Line> sourceFilesLines,
            ByteBuffer buffer) {
        
        this.simulationId = simulationId;
        if( this.simulationId==null ) {
            generateSimulationId();
        }
        
        if( sourceFiles!=null ) {
            this.sourceFiles.addAll(sourceFiles);
        }
        if( sourceFilesSource!=null ) {
            this.sourceFilesSource.putAll(sourceFilesSource);
        }
        if( sourceFilesLines!=null ) {
            this.sourceFilesLines.addAll(sourceFilesLines);
        }
        this.sketchName = sketchName;
        
        init();
        
        if( buffer != null ) {
            loadBinary = buffer.duplicate();
            try {
                ProgramMemoryLoader.binaryLoader().loadFlash(cpu.getFLASH(), buffer);
                cpu.getFLASH().decodeAll(cpu);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    private void init() {
        map.put(simulationId, this);
        cpu = new ATmega328P();
//        cpu.setLog(true);
        previousMemory = new int[cpu.getSRAM().size()];
        resetPreviousMemory();
        setLastContact();

        cpu.getUsart().addListener(new Usart.UsartListener() {
            @Override
            public void onChar(char c) {
                String json = Json.createObjectBuilder()
                        .add("type", "usart")
                        .add("value", c)
                        .build()
                        .toString();
                sendMessage(json);
            }
        });      
        
        cpu.addPinChangedListener(new CPU.PinChangedListener() {
            @Override
            public void pinChanged(int pin, int value) {
                String json = Json.createObjectBuilder()
                        .add("type", "pinChange")
                        .add("pin", pin)
                        .add("value", value)
                        .build()
                        .toString();
                sendMessage(json);
            }
        });
        cpu.addCPUStatusListener(new CPU.CPUStatusListener() {
            @Override
            public void statusChanged(boolean running) {
                status();
            }
        });
    }
    
    private void resetPreviousMemory() {
        for( int i = 0; i < previousMemory.length; i++ ) {
            previousMemory[i] = -1;
        }
    }

    public final void generateSimulationId() {
        simulationId = UUID.randomUUID().toString();
    }

    public String getSimulationId() {
        if( System.currentTimeMillis() - lastContact > 10 * 1000 ) {
            return "invalid";
        }
        return simulationId;
    }

    public static Simulation getSimulation(String id) {
        return map.get(id);
    }


    public final void setLastContact() {
        lastContact = System.currentTimeMillis();
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public void sendSerial(String str) {
        for( char c : str.toCharArray() ) {
            cpu.getUsart().write(c);
        }
    }

    public void sendSerial(char c) {
        cpu.getUsart().write(c);
    }

    public void ping() {
        String json = Json.createObjectBuilder()
                .add("type", "ping")
                .build()
                .toString();
        sendMessage(json);
    }

    public void addSourceFile(String filename, String content) {
        sourceFilesSource.put(filename, content);
        sendSource(filename, content);
    }
    
    private void sendSource(String filename, String content) {
        String json = Json.createObjectBuilder()
                .add("type", "sourceFile")
                .add("name", filename)
                .add("content", content)
                .build()
                .toString();
        sendMessage(json);
    }

    public List<DwLineNumberInformation.Line> getSourceLinesMapping() {
        return sourceFilesLines;
    }
    
    private void sendSourceLines() {
        JsonObjectBuilder builder = Json.createObjectBuilder()
                .add("type", "sourceLines");
        try {
            JsonArrayBuilder ab = Json.createArrayBuilder();
            for(DwLineNumberInformation.Line line : sourceFilesLines ) {
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
            builder.add("lines", ab);
        } catch(Exception e) {
        }
        String json = builder
                .build()
                .toString();
        sendMessage(json);
    }
    
    public void sendSourceFilesSource() {
        sendSourceLines();
        for(String filename : sourceFilesSource.keySet()) {
            sendSource(filename, sourceFilesSource.get(filename));
        }
    }
    
    public void sendCircuit() {
        String json = Json.createObjectBuilder()
                .add("type", "loadCircuit")
                .add("name", getName()==null?"":getName())
                .add("circuit", getCircuit()==null?"":getCircuit())
                .build()
                .toString();
        sendMessage(json);
    }
    
    public void setUserSimulations(Map<Long,String> simulationsList) {
        this.simulationsList = simulationsList;
    }
    
    public void sendUserSimulations() {
        JsonObjectBuilder builder = Json.createObjectBuilder()
                .add("type", "simulationsList");
        if( simulationsList!=null && !simulationsList.isEmpty() ) {
            try {
                JsonArrayBuilder ab = Json.createArrayBuilder();
                for( Long id : simulationsList.keySet() ) {
                    String name = simulationsList.get(id);
                    JsonObject obj = Json.createObjectBuilder()
                        .add("id", id)
                        .add("name", name)
                        .build();
                    ab.add(obj);
                }
                builder.add("list", ab);
            } catch(Exception e) {
                e.printStackTrace();
            }        
        }
        sendMessage(builder.build().toString());
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getUsername() {
        return username;
    }
    
    public List<DwLineNumberInformation.File> getSourceFiles() {
        return sourceFiles;
    }
    
    public List<DwLineNumberInformation.File> getSourceFilesToDownload() {
        return sourceFilesCopy;
    }

    public String getSourceFilesSource(String filename) {
        return sourceFilesSource.get(filename);
    }
    
    
    public List<DwLineNumberInformation.File> load(Load load) throws Exception {
        sourceFiles.clear();
        sourceFilesCopy.clear();
        sourceFilesSource.clear();
        sourceFilesLines.clear();
                
        boolean wasRunning = cpu.isRunning();
        if( wasRunning ) {
            //deve-se chamar o stop desta classe pois espera que a execução pare
            //o stop() espera que a thread do cpu termine
            stop();
        }
        resetPreviousMemory();
        cpu.removeAllBreakpoints();
        if( load.getFileName().endsWith("ino.hex") ) {
            ByteBuffer buf = ByteBuffer.wrap(load.getContent());
            //TODO: isto temn de ser transformado em binário
            loadBinary = buf.duplicate();
            ProgramMemoryLoader.intelHexLoader().loadFlash(cpu.getFLASH(), buf);
        } else if( load.getFileName().endsWith(".elf") ) {
            ByteBuffer buf = ByteBuffer.wrap(load.getContent());
            buf.order(ByteOrder.LITTLE_ENDIAN);
            Elf32Context elf = new Elf32Context(buf);
            ByteBuffer txtSection = elf.getSectionBufferByName(".text");
            ByteBuffer dataSection = elf.getSectionBufferByName(".data");
            //TODO: verificar o endereço de carregamento da .data section
            //      neste momento estamos a assumir que começa sempre no final da .text section
            //      o ELF indica essa posição nos "Program Headers" campo "physical address"
            if( dataSection.remaining()>0 ) {
                ByteBuffer buffer = ByteBuffer.allocate(txtSection.remaining()+dataSection.remaining());
                buffer.order(txtSection.order());
                buffer.put(txtSection);
                buffer.put(dataSection);
                buffer.flip();
                txtSection = buffer;
            }
            loadBinary = txtSection.duplicate();
            ProgramMemoryLoader.binaryLoader().loadFlash(cpu.getFLASH(), txtSection);

            Dwarf32Context dwarf = new Dwarf32Context(elf);
            for( CompilationUnit cu : dwarf.getCompilationUnits() ) {
                Map<Integer,DwLineNumberInformation.Line> lines = cu.getDwLineNumberInformation().getLines();
                sourceFilesLines.addAll(lines.values());
                for(DwLineNumberInformation.Line line : lines.values()) {
                    DwLineNumberInformation.File f = line.getFile();
                    if( !sourceFiles.contains(f) && !f.getDirectory().startsWith(".") ) {
                        sourceFiles.add(f);
                        System.out.println(sourceFiles);
                    }
                }
                sourceFilesCopy.addAll(sourceFiles);
            }
            sendSourceLines();
        } else {
            throw new Exception("invalid file type");
        }

        sketchName = load.getFileName();
        if( sketchName!=null && (sketchName.endsWith(".ino.hex")||sketchName.endsWith(".ino.elf")) ) {
            sketchName = sketchName.substring(0, sketchName.length()-8);
        }
        
        cpu.getFLASH().decodeAll(cpu);

        getASM();
        cpu.setIncInDisassemble(false);


        start();

        return sourceFiles;

    }

    public String getSketchName() {
        return sketchName;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public void setCircuit(String circuit) {
        this.circuit = circuit;
    }
    
    public String getCircuit() {
        return circuit;
    }
    
    public ByteBuffer getLoad() {
        if( loadBinary!=null ) {
            loadBinary.rewind();
        }
        return loadBinary;
    }
    
    public void start() {
        if( cpu.isRunning() ) {
            //deve-se chamar o stop desta classe pois espera que a execução pare
            stop();
        }
        cpu.reset();
        resume();
    }

    public void resume() {
        //usar o executerservice criado no ServletListener
        ExecutorService executer = ServletListener.getExecutorService();
        synchronized( executer ) {
            if( executer instanceof ThreadPoolExecutor ) {
                ThreadPoolExecutor ex = (ThreadPoolExecutor) executer;
                if( ex.getActiveCount() >= ex.getMaximumPoolSize() ) {
                    exception(new Exception("No available processor to execute the simulation. Try again later."));
                    return;
                }
            }
            task = executer.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        cpu.run();
                    } catch(InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } catch(Exception e) {
                        if( !(e instanceof BreakpointException) ) {
                            e.printStackTrace();
                        }
                        exception(e);
                    }
                }
            });
        }
    }

    public void stop() {
        cpu.stop();
        //esperar que o CPU pare a execução
        try {
            if( task != null ) {
                task.get();
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        task = null;
    }

    public void step() {
        try {
            //TODO: verificar se se deve ler o PC antes ou depois da execução da instrução
            //      adicionei o getPreviousPc mas acho que deu problemas 
            //      no debug (a linha no código fonte parece andar atrasada)
            int pc = cpu.getPc();
            cpu.execute();
            String json = Json.createObjectBuilder()
                    .add("type", "step")
                    .add("address", String.format("%04X", cpu.getPreviousPc()))
                    .build()
                    .toString();
            sendMessage(json);
        } catch(Exception e) {
            e.printStackTrace();
            exception(e);
        }
        getDataMemory();
    }

    public void status() {
        String json = Json.createObjectBuilder()
                .add("type", "statusChange")
                .add("status", cpu.isRunning() ? "running" : "stopped")
                .add("address", cpu.getPreviousPc())
                .add("sketchName", sketchName == null ? "" : sketchName)
                .add("simulationId", simulationId)
                .build()
                .toString();
        sendMessage(json);
    }

    public void getASM() {
        JsonObjectBuilder builder = Json.createObjectBuilder()
                .add("type", "asm");
        try {
            List<String> list = cpu.disassemble();
            JsonArrayBuilder ab = Json.createArrayBuilder();
            for( String inst : list ) {
                System.out.println(inst);
                ab.add(inst);
            }
            builder.add("asm", ab);
        } catch(Exception e) {
            e.printStackTrace();
        }
        String json = builder
                .build()
                .toString();
        sendMessage(json);
        toogleBreakpoint(-1);
    }

    public void getDataMemory() {
        getDataMemory(false);
    }

    public void getDataMemory(boolean all) {
        JsonObjectBuilder builder = Json.createObjectBuilder()
                .add("type", "dataMemory");
        try {
            JsonArrayBuilder m = Json.createArrayBuilder();
            for( int i = 0; i < cpu.getSRAM().size(); i++ ) {
                int value = cpu.getSRAM().get(i, true);
                if( all || value != previousMemory[i] ) {
                    previousMemory[i] = value;
                    JsonObjectBuilder ad = Json.createObjectBuilder()
                            .add("address", i)
                            .add("value", value);
                    m.add(ad);
                }
            }
            builder.add("memory", m);
        } catch(Exception e) {
        }
        String json = builder
                .build()
                .toString();
        sendMessage(json);
    }

    public void toogleBreakpoint(int address) {
        List<Integer> breakpoints = cpu.getBreakpoints();
        if( address != -1 ) {
            if( breakpoints.contains(address) ) {
                cpu.removeBreakpoint(address);
            } else {
                cpu.addBreakpoint(address);
            }
            breakpoints = cpu.getBreakpoints();
        }
        JsonObjectBuilder builder = Json.createObjectBuilder()
                .add("type", "breakpoints");
        try {
            JsonArrayBuilder ab = Json.createArrayBuilder();
            for( Integer breakpoint : breakpoints ) {
                ab.add(breakpoint);
            }
            builder.add("breakpoints", ab);
        } catch(Exception e) {
        }
        String json = builder
                .build()
                .toString();
        sendMessage(json);
    }

    public void speed() {
        String json = Json.createObjectBuilder()
                .add("type", "speed")
                .add("speed", cpu.getSpeed())
                .build()
                .toString();
        sendMessage(json);
    }

    public void exception(Exception e) {
        JsonObjectBuilder jsonBuilder = Json.createObjectBuilder()
                .add("type", "exception")
                .add("exception", e.getClass().getSimpleName())
                .add("address", String.format("%04X", cpu.getPc()));
        if( e.getMessage() != null ) {
            jsonBuilder.add("message", e.getMessage());
        }
        sendMessage(jsonBuilder.build().toString());
    }

    private synchronized void sendMessage(String message) {
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

    public void shutdown() {
        //terminar todas as threads
        System.out.println("stopping simulation " + simulationId);
        map.remove(simulationId);
        stop();
        cpu.shutdown();
    }

    
    public void pinChange(int pin, Float value) {
        cpu.setInputPinValue(pin, value);
    }

    
    long authenticationLogId = -1;
    @Override
    public void valueBound(HttpSessionBindingEvent event) {
        authenticationLogId = (Long) event.getSession().getAttribute("authentication_log_id");
    }

    @Override
    public void valueUnbound(HttpSessionBindingEvent event) {

        try {
            pt.isec.deis.mis.arduinosimulatorweb.LogoutServlet.registerLogout(authenticationLogId, 2);
        } catch(Exception e) {
            e.printStackTrace();
        }
        shutdown();
    }
}
