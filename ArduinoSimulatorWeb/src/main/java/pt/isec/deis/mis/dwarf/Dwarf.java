/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.isec.deis.mis.dwarf;

import com.bicirikdwarf.dwarf.CompilationUnit;
import com.bicirikdwarf.dwarf.DebugInfoEntry;
import com.bicirikdwarf.dwarf.DwAtType;
import com.bicirikdwarf.dwarf.DwTagType;
import com.bicirikdwarf.dwarf.Dwarf32Context;
import com.bicirikdwarf.elf.Elf32Context;
import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 *
 * @author pafgoncalves@ipc.pt
 */
public class Dwarf {

    Dwarf32Context dwarf;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        // TODO code application logic here
        
//        File elfFile = new File("/tmp/arduino_build_164592/botao.ino.elf");
        File elfFile = new File("botao.ino.elf");
        
        RandomAccessFile aFile = new RandomAccessFile(elfFile, "r");
        FileChannel inChannel = aFile.getChannel();
        MappedByteBuffer buffer = inChannel.map(FileChannel.MapMode.READ_ONLY, 0, inChannel.size());
        
        /*
        da memória:
        byte[] data = new byte[count];
        ByteBuffer result = ByteBuffer.wrap(data);
        */
        
        Dwarf dwarf = new Dwarf(buffer);
        
    }

    public Dwarf(ByteBuffer buffer) throws Exception {
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        Elf32Context elf = new Elf32Context(buffer);
        dwarf = new Dwarf32Context(elf);
        
        for(CompilationUnit cu : dwarf.getCompilationUnits()) {
            System.out.println("\n\n");
            System.out.println("cu.pointer_size: "+cu.pointer_size);
            showDebugInfoEntry(cu.getCompileUnit(), "");
            for(DebugInfoEntry entry : cu.getCompileUnit().getChildren()) {
                showDebugInfoEntry(entry, "   ");
            }
        }
    }
    
    public Dwarf32Context getDwarf32Context() {
        return dwarf;
    }
    
    private void showDebugInfoEntry(DebugInfoEntry entry, String pad) {
        System.out.println(pad+entry.getAbbrev().tag+" "+entry.address);
        System.out.print(pad+"   attributes: ");
        boolean first = true;
        for(DwAtType attrib : entry.getAttribs()) {
            if( !first ) {
                System.out.print(", ");
            }
            System.out.print(attrib);
            first = false;
        }
        System.out.println("");
        if( entry.getAbbrev().tag==DwTagType.DW_TAG_compile_unit ) {
            showCompilationUnit(entry, pad+"      ");
        } else if( entry.getAbbrev().tag==DwTagType.DW_TAG_variable || entry.getAbbrev().tag==DwTagType.DW_TAG_formal_parameter ) {
            showVariable(entry, pad+"      ");
        } else if( entry.getAbbrev().tag==DwTagType.DW_TAG_subprogram || entry.getAbbrev().tag==DwTagType.DW_TAG_lexical_block ) {
            showSubprogram(entry, pad+"      ");
        }
        for(DebugInfoEntry subEntry : entry.getChildren()) {
            showDebugInfoEntry(subEntry, pad+"   ");
        }
    }
    
    private void showVariable(DebugInfoEntry entry, String pad) {
        System.out.println(pad+"name: "+entry.getAttribValue(DwAtType.DW_AT_name));
        System.out.println(pad+"file: "+entry.getAttribValue(DwAtType.DW_AT_decl_file));
        System.out.println(pad+"line: "+entry.getAttribValue(DwAtType.DW_AT_decl_line));
        if( entry.getAttribValue(DwAtType.DW_AT_type)!=null ) {
            System.out.println(pad+"type: "+getType( (int)entry.getAttribValue(DwAtType.DW_AT_type)));
        }
        //System.out.println(pad+"location: "+entry.getAttribValue(DwAtType.DW_AT_location));
    }
    
    private void showSubprogram(DebugInfoEntry entry, String pad) {
        System.out.println(pad+"name: "+entry.getAttribValue(DwAtType.DW_AT_name));
        Integer lowPc = (Integer)entry.getAttribValue(DwAtType.DW_AT_low_pc);
        if( lowPc!=null ) {
            System.out.println(pad+"low_pc: 0x"+Integer.toHexString(lowPc));
        }
        Integer highPc = (Integer)entry.getAttribValue(DwAtType.DW_AT_high_pc);
        if( highPc!=null ) {
            System.out.println(pad+"high_pc: 0x"+Integer.toHexString(highPc));
        }
    }

    private void showCompilationUnit(DebugInfoEntry entry, String pad) {
        System.out.println(pad+"producer: "+entry.getAttribValue(DwAtType.DW_AT_producer));
        System.out.println(pad+"DW_AT_stmt_list: "+entry.getAttribValue(DwAtType.DW_AT_stmt_list));
        if( entry.getAttribValue(DwAtType.DW_AT_language)!=null ) {
            System.out.println(pad+"language: "+entry.getAttribValue(DwAtType.DW_AT_language));
        }
        if( entry.getAttribValue(DwAtType.DW_AT_name)!=null ) {
            System.out.println(pad+"name: "+entry.getAttribValue(DwAtType.DW_AT_name));
        }
        if( entry.getAttribValue(DwAtType.DW_AT_comp_dir)!=null ) {
            System.out.println(pad+"comp_dir: "+entry.getAttribValue(DwAtType.DW_AT_comp_dir));
        }
        if( entry.getAttribValue(DwAtType.DW_AT_low_pc)!=null ) {
            System.out.println(pad+"low_pc: 0x"+Integer.toHexString((int)entry.getAttribValue(DwAtType.DW_AT_low_pc)));
        }
        if( entry.getAttribValue(DwAtType.DW_AT_high_pc)!=null ) {
            System.out.println(pad+"high_pc: 0x"+Integer.toHexString((int)entry.getAttribValue(DwAtType.DW_AT_high_pc)));
        }
    }
    
    private String getType(int address) {
        DebugInfoEntry entry = dwarf.getDieByAddress(address);
        if( entry.getAbbrev().tag==DwTagType.DW_TAG_const_type ) {
            return "const "+getType((int)entry.getAttribValue(DwAtType.DW_AT_type));
        }
        if( entry.getAbbrev().tag==DwTagType.DW_TAG_volatile_type ) {
            return "volatile "+getType((int)entry.getAttribValue(DwAtType.DW_AT_type));
        }
        if( entry.getAbbrev().tag==DwTagType.DW_TAG_pointer_type ) {
            return getType((int)entry.getAttribValue(DwAtType.DW_AT_type))+" *";
        }
        if( entry.getAbbrev().tag==DwTagType.DW_TAG_array_type ) {
            return getType((int)entry.getAttribValue(DwAtType.DW_AT_type))+"[]";
        }
        if( entry.getAbbrev().tag==DwTagType.DW_TAG_base_type || entry.getAbbrev().tag==DwTagType.DW_TAG_typedef ) {
            //foi o fim
//            return entry.getAttribValue(DwAtType.DW_AT_encoding).toString()+" "+entry.getAttribValue(DwAtType.DW_AT_name).toString();
            String signal = "";
            if( entry.getAttribValue(DwAtType.DW_AT_encoding)!=null ) {
                signal = entry.getAttribValue(DwAtType.DW_AT_encoding).toString();
            }
            if( signal!=null && signal.equals("unsigned") ) {
                signal = signal+" ";
            } else {
                signal = "";
            }
            return signal+entry.getAttribValue(DwAtType.DW_AT_name).toString();
        }
        String type = entry.getAbbrev().tag.toString();
        String name = (String)entry.getAttribValue(DwAtType.DW_AT_name);
        DebugInfoEntry subtype = null;
        if( entry.getAttribValue(DwAtType.DW_AT_type)!=null ) {
            subtype = dwarf.getDieByAddress((int)entry.getAttribValue(DwAtType.DW_AT_type));
        }
        return type+" <"+name+">"+(subtype!=null?subtype.getAbbrev().tag.toString():"");
    }
    
}
