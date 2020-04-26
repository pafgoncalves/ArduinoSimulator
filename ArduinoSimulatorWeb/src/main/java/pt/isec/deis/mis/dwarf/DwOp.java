/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.isec.deis.mis.dwarf;

import com.bicirikdwarf.dwarf.DwOpType;
import java.nio.ByteBuffer;

/**
 *
 * @author pafgoncalves@ipc.pt
 */
public class DwOp {

    private final ByteBuffer buf;
    
    public DwOp(ByteBuffer buf) {
        this.buf = buf;
    }
    
    
    public long getAddress() {
        long value = -1;
        buf.mark();
        DwOpType type = DwOpType.byValue(buf.get());
        switch(type) {
            case DW_OP_addr:
                value = buf.getInt();
                break;
            default:
                System.out.println("++++++ operação não implementada: "+type.name());
                /*
                ++++++ operação não implementada: DW_OP_reg18
                ++++++ operação não implementada: DW_OP_reg8
                ++++++ operação não implementada: DW_OP_reg16
                */
                break;
        }
        buf.reset();
        return value;
    }
    
    
}
