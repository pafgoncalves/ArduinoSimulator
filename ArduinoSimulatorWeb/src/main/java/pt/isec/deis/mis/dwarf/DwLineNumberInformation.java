/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.isec.deis.mis.dwarf;

import com.bicirikdwarf.utils.ElfUtils;
import com.bicirikdwarf.utils.Leb128;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 *
 * @author pafgoncalves@ipc.pt
 */
public class DwLineNumberInformation {

    protected ByteBuffer debugLineSection;

    protected List<String> directoryTable = new ArrayList<>();
    protected List<File> fileTable = new ArrayList<>();
    protected Map<Integer, Line> table = new TreeMap<>();
    
    protected int ponterSize;
    protected int offset;

    public DwLineNumberInformation(int ponterSize, int offset, ByteBuffer section) {
        this.debugLineSection = section;
        this.ponterSize = ponterSize;
        this.offset = offset;
        init();
    }

    private void init() {
        debugLineSection.position(offset);
        
        System.out.println("start = " + offset);
        CompileUnitHeader header = new CompileUnitHeader();
        header.read(debugLineSection);
        System.out.println(header);

        int limit = (int) header.totalLength + 4;
        System.out.println("\ntamanho: " + limit);
        System.out.println("position: " + debugLineSection.position());
        System.out.println("remaining: " + debugLineSection.remaining());
        debugLineSection.limit(offset + limit);

        System.out.println("remaining after limit: " + debugLineSection.remaining());
        String directory;
        while( (directory = ElfUtils.getNTString(debugLineSection)).length() > 0 ) {
            directoryTable.add(directory);
        }
        System.out.println(directoryTable);

        System.out.println("before file: " + debugLineSection.remaining());
        while( debugLineSection.hasRemaining() ) {
            File file = new File();
            file.filename = ElfUtils.getNTString(debugLineSection);
            if( file.filename.isEmpty() ) {
                break;
            }
            int directoryIndex = (int) Leb128.getULEB128(debugLineSection);
            if( directoryIndex > 0 ) {
                file.directory = directoryTable.get(directoryIndex - 1);
            }
            file.lastModificationTime = Leb128.getULEB128(debugLineSection);
            file.size = Leb128.getULEB128(debugLineSection);
            fileTable.add(file);
            System.out.println("file: " + file);
        }

        StateMachineRegisters smr = new StateMachineRegisters(header);
        smr.read(debugLineSection);

        for(Line l : table.values()) {
            System.out.println(l);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("directories:\n");
        for(String dir : directoryTable) {
            sb.append(dir).append("\n");
        }
        sb.append("\nfiles:\n");
        for(File file : fileTable) {
            sb.append(file.directory).append(java.io.File.separator).append(file.filename).append("\n");
        }
        sb.append("\naddress\tline\tcolumn\tstmt start\tblock start\tfile\n");
        for(Line l : table.values()) {
            sb.append(l).append("\n");
        }
        return sb.toString();
    }
    
    public static void main(String[] args) throws Exception {
        java.io.RandomAccessFile aFile = new java.io.RandomAccessFile("botao.ino.elf", "r");
        java.nio.channels.FileChannel inChannel = aFile.getChannel();
        java.nio.MappedByteBuffer buffer = inChannel.map(java.nio.channels.FileChannel.MapMode.READ_ONLY, 0, inChannel.size());
        Dwarf dwarf = new Dwarf(buffer);
    }

    public List<String> getDirectories() {
        return directoryTable;
    }

    public List<File> getFiles() {
        return fileTable;
    }

    public Map<Integer, Line> getLines() {
        return table;
    }

    private class CompileUnitHeader {

        protected long totalLength;
        protected int version;
        protected int prologueLength;
        protected int minimumInstructionLength;
        protected boolean defaultIsStmt;
        protected int lineBase;
        protected int lineRange;
        protected int opcodeBase;
        protected byte[] standardOpcodeLengths;

        public void read(ByteBuffer buf) {
            totalLength = (long) buf.getInt() & 0xFFFFFFFFL;
            version = buf.getShort() & 0xFFFF;
            prologueLength = (int) buf.getInt() & 0xFFFFFFFF;
            minimumInstructionLength = buf.get() & 0xFF;
            defaultIsStmt = buf.get() != 0;
            lineBase = buf.get();
            lineRange = buf.get() & 0xFF;
            opcodeBase = buf.get() & 0xFF;
            standardOpcodeLengths = new byte[opcodeBase - 1];
            buf.get(standardOpcodeLengths);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("totalLength: ").append(totalLength).append("\n");
            sb.append("version: ").append(version).append("\n");
            sb.append("prologueLength: ").append(prologueLength).append("\n");
            sb.append("minimumInstructionLength: ").append(minimumInstructionLength).append("\n");
            sb.append("defaultIsStmt: ").append(defaultIsStmt).append("\n");
            sb.append("lineBase: ").append(lineBase).append("\n");
            sb.append("lineRange: ").append(lineRange).append("\n");
            sb.append("opcodeBase: ").append(opcodeBase).append("\n");
            sb.append("standardOpcodeLengths: [");
            for( int i = 0; i < standardOpcodeLengths.length; i++ ) {
                if( i > 0 ) {
                    sb.append(",");
                }
                sb.append(standardOpcodeLengths[i]);
            }
            sb.append("]");
            return sb.toString();
        }

    }

    private class StateMachineRegisters {

        protected int address;
        protected int file;
        protected int line;
        protected int column;
        protected boolean isStmt;
        protected boolean isBasicBlock;
        protected boolean isEndSequence;

        protected CompileUnitHeader header;
        
        private static final int DW_LNS_extended_op = 0;
        private static final int DW_LNS_copy = 1;
        private static final int DW_LNS_advance_pc = 2;
        private static final int DW_LNS_advance_line = 3;
        private static final int DW_LNS_set_file = 4;
        private static final int DW_LNS_set_column = 5;
        private static final int DW_LNS_negate_stmt = 6;
        private static final int DW_LNS_set_basic_block = 7;
        private static final int DW_LNS_const_add_pc = 8;
        private static final int DW_LNS_fixed_advance_pc = 9;
        
        private static final int DW_LNE_end_sequence = 1;
        private static final int DW_LNE_set_address = 2;
        private static final int DW_LNE_define_file = 3;

        public StateMachineRegisters(CompileUnitHeader header) {
            this.header = header;
            reset();
        }

        public final void reset() {
            address = 0;
            file = 1;
            line = 1;
            column = 0;
            isStmt = header.defaultIsStmt;
            isBasicBlock = false;
            isEndSequence = false;
        }
        
        protected void addLine() {
            Line newLine = new Line();
            newLine.address = address;
            if( file>0 ) {  //se for 0 representa a directoria corrente
                newLine.file = fileTable.get(file-1);
            }
            newLine.isBasicBlockStart = isBasicBlock;
            newLine.isSourceStatementStart = isStmt;
            newLine.sourceColumn = column;
            newLine.sourceLine = line;
            table.put(address, newLine);
        }

        public void read(ByteBuffer program) {
//            while( program.hasRemaining() ) program.get();
            while( program.hasRemaining() ) {
                System.out.print("pos: 0x"+Integer.toHexString(program.position())+" ");
                int opcode = program.get() & 0xFF;
                if( opcode < header.opcodeBase ) {
                    //Standard Opcodes
                    System.out.print("opcode = "+opcode+" ");
                    switch( opcode ) {
                        case DW_LNS_extended_op:
                            {
                                //Extended Opcodes
                                long size = Leb128.getULEB128(program);
                                int subOpcode = program.get() & 0xFF;
                                switch(subOpcode) {
                                    case DW_LNE_end_sequence:
                                        System.out.println("DW_LNE_end_sequence");
                                        isEndSequence = true;
//                                        addLine();
                                        reset();
                                        break;
                                        
                                    case DW_LNE_set_address:
                                        address = ElfUtils.toInteger(program, ponterSize);
                                        System.out.println("DW_LNE_set_address 0x"+Integer.toHexString(address));
                                        break;
                                        
                                    case DW_LNE_define_file:
                                        ElfUtils.getNTString(program);
                                        File newFile = new File();
                                        newFile.filename = ElfUtils.getNTString(program);
                                        int directoryIndex = (int) Leb128.getULEB128(program);
                                        if( directoryIndex > 0 ) {
                                            newFile.directory = directoryTable.get(directoryIndex - 1);
                                        }
                                        newFile.lastModificationTime = Leb128.getULEB128(program);
                                        newFile.size = Leb128.getULEB128(program);
                                        fileTable.add(newFile);
                                        System.out.println("file: "+newFile.getFilename());
                                        break;
                                        
                                    default:
                                        program.position(program.position()+(int)size-1);
                                }
                            }
                            break;
                            
                        case DW_LNS_copy:
                            System.out.println("DW_LNS_copy");
                            addLine();
                            this.isBasicBlock = false;
                            break;
                            
                        case DW_LNS_advance_pc:
                            int v = (int)Leb128.getULEB128(program);
                            System.out.print("DW_LNS_advance_pc "+(v * header.minimumInstructionLength));
                            address += v * header.minimumInstructionLength;
                            System.out.println(" para 0x"+Integer.toHexString(address));
                            break;
                            
                        case DW_LNS_advance_line:
                            int l = Leb128.getSLEB128(program);
                            System.out.print("DW_LNS_advance_line "+(l));
                            line += l;
                            System.out.println(" para "+line);
                            break;
                            
                        case DW_LNS_set_file:
                            file = (int)Leb128.getULEB128(program);
                            System.out.println("DW_LNS_set_file "+file);
                            break;
                            
                        case DW_LNS_set_column:
                            column = (int)Leb128.getULEB128(program);
                            System.out.println("DW_LNS_set_column "+column);
                            break;
                            
                        case DW_LNS_negate_stmt:
                            isStmt = !isStmt;
                            System.out.println("DW_LNS_negate_stmt "+isStmt);
                            break;
                            
                        case DW_LNS_set_basic_block:
                            isBasicBlock = true;
                            System.out.println("DW_LNS_set_basic_block");
                            break;
                            
                        case DW_LNS_const_add_pc:
                            System.out.print("DW_LNS_const_add_pc "+((255-header.opcodeBase)/header.lineRange));
                            address += (255-header.opcodeBase)/header.lineRange;
                            System.out.println(" para 0x"+Integer.toHexString(address));
                            break;
                            
                        case DW_LNS_fixed_advance_pc:
                            int a = program.getShort()&0xFFFF;
                            System.out.print("DW_LNS_fixed_advance_pc "+a);
                            address += a;
                            System.out.println(" para 0x"+Integer.toHexString(address));
                            break;
                    }
                } else {    //special opcodes
                    System.out.println("special opcode "+opcode);
                    int adjustedOpcode = opcode - header.opcodeBase;
                    int addressIncrement = adjustedOpcode / header.lineRange;
                    int lineIncrement = header.lineBase + (adjustedOpcode % header.lineRange);
                    
                    address += addressIncrement;
                    line += lineIncrement;
                    
                    addLine();
                    this.isBasicBlock = false;
                }
            }
        }
    }

    public static class File {

        protected String filename;
        protected String directory;
        protected long lastModificationTime;
        protected long size;

        
        protected File() {}
        
        public File(String filename, String directory, long lastModificationTime, long size) {
            this.filename = filename;
            this.directory = directory;
            this.lastModificationTime = lastModificationTime;
            this.size = size;
        }
        
        public String getFilename() {
            return filename;
        }

        public String getDirectory() {
            return directory;
        }

        public long getLastModificationTime() {
            return lastModificationTime;
        }

        public long getSize() {
            return size;
        }

        @Override
        public String toString() {
            return filename + " {size=" + size + ", lastModificationTime=" + lastModificationTime + ", directory=" + directory + "}";
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 53 * hash + Objects.hashCode(this.filename);
            hash = 53 * hash + Objects.hashCode(this.directory);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if( this == obj ) {
                return true;
            }
            if( obj == null ) {
                return false;
            }
            if( getClass() != obj.getClass() ) {
                return false;
            }
            final File other = (File) obj;
            if( !Objects.equals(this.filename, other.filename) ) {
                return false;
            }
            if( !Objects.equals(this.directory, other.directory) ) {
                return false;
            }
            return true;
        }
        
        
    }

    public static class Line {

        protected int address;
        protected File file;
        protected int sourceLine;
        protected int sourceColumn;
        protected boolean isSourceStatementStart;
        protected boolean isBasicBlockStart;

        protected Line() {}
        
        public Line(int address, File file, int sourceLine) {
            this.address = address;
            this.file = file;
            this.sourceLine = sourceLine;
            sourceColumn = 0;
            isSourceStatementStart = false;
            isBasicBlockStart = false;
        }
        
        public int getAddress() {
            return address;
        }

        public File getFile() {
            return file;
        }

        public int getSourceLine() {
            return sourceLine;
        }

        public int getSourceColumn() {
            return sourceColumn;
        }

        public boolean isSourceStatementStart() {
            return isSourceStatementStart;
        }

        public boolean isBasicBlockStart() {
            return isBasicBlockStart;
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            String str = Long.toHexString(address);
            while(str.length()<4) {
                str = "0"+str;
            }
            sb.append("0x").append(str.toUpperCase()).append("\t");
            sb.append(sourceLine).append("\t");
            sb.append(sourceColumn).append("\t");
            sb.append(isSourceStatementStart).append("\t");
            sb.append(isBasicBlockStart).append("\t");
            sb.append(file.filename);
            return sb.toString();
        }
    }
}
