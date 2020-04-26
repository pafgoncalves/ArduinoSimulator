/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.isec.deis.mis.dwarf;


import java.io.File;

/**
 *
 * @author pafgoncalves@ipc.pt
 */
public class DwarfWrapper {
    
    private final Dwarf dwarf;
    private final File file;
    
    public DwarfWrapper(Dwarf dwarf, File file) {
        this.dwarf = dwarf;
        this.file = file;
    }
    
    public String getText() {
        StringBuilder sb = new StringBuilder();
        sb.append(file.getName());
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return file.getName();
    }
    
}
