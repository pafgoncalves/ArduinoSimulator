/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.isec.deis.mis.dwarf;

import com.bicirikdwarf.dwarf.DebugInfoEntry;
import com.bicirikdwarf.dwarf.DwAtType;
import com.bicirikdwarf.dwarf.DwAteType;
import com.bicirikdwarf.dwarf.DwTagType;

/**
 *
 * @author pafgoncalves@ipc.pt
 */
public class DebugInfoEntryWrapper {
    
    private final DebugInfoEntry entry;
    
    public DebugInfoEntryWrapper(DebugInfoEntry entry) {
        this.entry = entry;
    }
    
    public String getText() {
        StringBuilder sb = new StringBuilder();
        sb.append(entry.getAbbrev().tag).append("\n");
        sb.append("address: ").append(entry.address).append("\n");
        sb.append("attributes:\n");
        for(DwAtType attrib : entry.getAttribs()) {
            sb.append("   ");
            if( attrib!=null ) {
                sb.append(attrib).append(":\t").append(getAttributeValue(entry,attrib)).append("\n");
            } else {
                sb.append("null attribute!!\n");
            }
        }
        sb.append("\n");
        if( entry.getAbbrev().tag==DwTagType.DW_TAG_variable || entry.getAbbrev().tag==DwTagType.DW_TAG_formal_parameter ) {
            if( entry.getAttribValue(DwAtType.DW_AT_type)!=null ) {
                sb.append(getType( (int)entry.getAttribValue(DwAtType.DW_AT_type))).append(" ");
            }
            sb.append(entry.getAttribValue(DwAtType.DW_AT_name)).append(";");
        }

        if( entry.getAbbrev().tag==DwTagType.DW_TAG_subprogram ) {
            if( entry.getAttribValue(DwAtType.DW_AT_type)!=null ) {
                sb.append(getType( (int)entry.getAttribValue(DwAtType.DW_AT_type))).append(" ");
            } else {
                sb.append("void ");
            }
            if( entry.getAttribValue(DwAtType.DW_AT_name)!=null ) {
                sb.append(entry.getAttribValue(DwAtType.DW_AT_name)).append("(");
            } else {
                sb.append("NONAME(");
            }
            boolean hasParameters = false;
            for(DebugInfoEntry child : entry.getChildren()) {
                if( child.getAbbrev().tag==DwTagType.DW_TAG_formal_parameter ) {
                    if( child.getAttribValue(DwAtType.DW_AT_type) != null ) {
                        sb.append(getType((int)child.getAttribValue(DwAtType.DW_AT_type)));
                        sb.append(" ");
                        sb.append(child.getAttribValue(DwAtType.DW_AT_name));
                        sb.append(", ");
                        hasParameters = true;
                    }
                }
            }
            if( hasParameters ) {
                sb.delete(sb.length()-2, sb.length());
            }
            sb.append(") {\n");
            sb.append("}\n");
            
        }
        
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return entry.getAbbrev().tag.toString()+" ["+entry.address+"]";
    }
    
    public DebugInfoEntry getDebugInfoEntry() {
        return entry;
    }
    
    private String getType(int address) {
        DebugInfoEntry typeEntry = entry.getDwarfContext().getDieByAddress(address);
        if( typeEntry.getAbbrev().tag==DwTagType.DW_TAG_const_type ) {
            return getType((int)typeEntry.getAttribValue(DwAtType.DW_AT_type))+" const";
        }
        if( typeEntry.getAbbrev().tag==DwTagType.DW_TAG_volatile_type ) {
            return getType((int)typeEntry.getAttribValue(DwAtType.DW_AT_type))+" volatile";
        }
        if( typeEntry.getAbbrev().tag==DwTagType.DW_TAG_pointer_type ) {
            return getType((int)typeEntry.getAttribValue(DwAtType.DW_AT_type))+" *";
        }
        if( typeEntry.getAbbrev().tag==DwTagType.DW_TAG_array_type ) {
            String size = "";
            DebugInfoEntry range = typeEntry.getChild(DwTagType.DW_TAG_subrange_type);
            if( range!=null ) {
                Object up = range.getAttribValue(DwAtType.DW_AT_upper_bound);
                if( up!=null ) {
                    size = (objectToNumber(up)+1)+"";
                }
            }
            return getType((int)typeEntry.getAttribValue(DwAtType.DW_AT_type))+"["+size+"]";
        }
        if( typeEntry.getAbbrev().tag==DwTagType.DW_TAG_base_type || typeEntry.getAbbrev().tag==DwTagType.DW_TAG_typedef ) {
            //foi o fim
            /*
            //o enconding só interessa para o debugger mostrar o valor (para saber se mostra com sinal ou não)
            String signal = "";
            if( typeEntry.getAttribValue(DwAtType.DW_AT_encoding)!=null ) {
                //signal = typeEntry.getAttribValue(DwAtType.DW_AT_encoding).toString();
                DwAteType enc = DwAteType.byValue((int)typeEntry.getAttribValue(DwAtType.DW_AT_encoding));
                signal = enc.toString().substring(7)+" ";
            }
            */
            return /*signal+*/typeEntry.getAttribValue(DwAtType.DW_AT_name).toString();
        }
        String type = typeEntry.getAbbrev().tag.toString();
        String name = (String)typeEntry.getAttribValue(DwAtType.DW_AT_name);
        DebugInfoEntry subtype = null;
        if( typeEntry.getAttribValue(DwAtType.DW_AT_type)!=null ) {
            subtype = entry.getDwarfContext().getDieByAddress((int)typeEntry.getAttribValue(DwAtType.DW_AT_type));
        }
        return type+" <"+name+">"+(subtype!=null?subtype.getAbbrev().tag.toString():"");
    }    
    
    
    private String getAttributeValue(DebugInfoEntry ent, DwAtType attrib) {
        Object obj = ent.getAttribValue(attrib);
        if( obj==null ) {
            return "null";
        }
        switch(attrib) {
            case DW_AT_language:
                switch( (int)objectToNumber(obj) ) {
                    case 1:
                        return "DW_LANG_C89";
                    case 2:
                        return "DW_LANG_C";
                    case 3:
                        return "DW_LANG_Ada83";
                    case 4:
                        return "DW_LANG_C_plus_plus";
                    case 5:
                        return "DW_LANG_Cobol74";
                    case 6:
                        return "DW_LANG_Cobol85";
                    case 7:
                        return "DW_LANG_Fortran77";
                    case 8:
                        return "DW_LANG_Fortran90";
                    case 9:
                        return "DW_LANG_Pascal83";
                    case 10:
                        return "DW_LANG_Modula2";
                }
                break;
                
            case DW_AT_low_pc:
            case DW_AT_high_pc:
                return toHex((int)obj);
                
            case DW_AT_decl_file:
                //se diferente de 0, aponta para Line Number Information (secção .debug_line)
                long index = objectToNumber(obj);
                if( index>0 ) {
//                    String filename = entry.getDwarfContext().getNameFinder().getFilename(((int)index)-1);
                    String filename = entry.getCompilationUnit().getDwLineNumberInformation().getFiles().get(((int)index)-1).filename;
                    if( filename!=null ) {
                        return filename;
                    } else {
                        return "null filename: "+obj.toString();
                    }
                } else {
                    return obj.toString();
                }
                
            case DW_AT_encoding:
                return DwAteType.byValue((int)obj).toString();
                
            case DW_AT_location:
                if( obj instanceof java.nio.ByteBuffer) {
                    java.nio.ByteBuffer buf = (java.nio.ByteBuffer)obj;
                    DwOp op = new DwOp(buf);
                    long addr = op.getAddress();
                    String str;
                    if( addr==-1 ) {
                        str = "size: "+buf.remaining();
                        if( buf.remaining()<10 ) {
                            str += " ";
                            buf.mark();
                            while( buf.remaining()>0 )
                                str += toHex(buf.get())+" ";
                            buf.reset();
                        }
                    } else {
                        str = toHex(addr);
                    }
                    return str;
                } else {
                    System.out.println("++++++ localização: "+obj.toString());
                }
                
        }
        return obj.toString();
    }
    
    private String toHex(long value) {
        String str = Long.toHexString(value);
        while(str.length()%2!=0) {
            str = "0"+str;
        }
        return "0x"+str.toUpperCase();
    }
    
    private long objectToNumber(Object obj) {
        if( obj instanceof Short) {
            return (short)obj;
        }
        if( obj instanceof Integer) {
            return (int)obj;
        }
        if( obj instanceof Long) {
            return (long)obj;
        }
        return -1;
    }
}
