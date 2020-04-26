/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.isec.deis.mis.arduinosimulator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 *
 * @author pafgoncalves@ipc.pt
 */
public abstract class ProgramMemoryLoader {

    public static ProgramMemoryLoader intelHexLoader() {
        return new IntelHexProgramMemoryLoader();
    }

    public static ProgramMemoryLoader binaryLoader() {
        return new BinaryProgramMemoryLoader();
    }

    public void loadFlash(ProgramMemory pm, File file) throws IOException {
        RandomAccessFile aFile = new RandomAccessFile(file, "r");
        FileChannel inChannel = aFile.getChannel();
        long fileSize = inChannel.size();
        ByteBuffer buffer = ByteBuffer.allocate((int) fileSize);
        inChannel.read(buffer);
        buffer.flip();
        ProgramMemoryLoader.this.loadFlash(pm, buffer);
    }

    public void loadEeprom(ProgramMemory pm, File file) throws IOException {
        RandomAccessFile aFile = new RandomAccessFile(file, "r");
        FileChannel inChannel = aFile.getChannel();
        long fileSize = inChannel.size();
        ByteBuffer buffer = ByteBuffer.allocate((int) fileSize);
        inChannel.read(buffer);
        buffer.flip();
        ProgramMemoryLoader.this.loadEeprom(pm, buffer);
    }

    public abstract void loadFlash(ProgramMemory pm, ByteBuffer buf) throws IOException;

    public abstract void loadEeprom(ProgramMemory pm, ByteBuffer buf) throws IOException;
}
