/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.isec.deis.mis.arduinosimulatorweb;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 *
 * @author pafgoncalves@ipc.pt
 */
//https://stackoverflow.com/questions/4332264/wrapping-a-bytebuffer-with-an-inputstream
public class ByteBufferBackedInputStream extends InputStream {

    ByteBuffer buf;

    public ByteBufferBackedInputStream(ByteBuffer buf) {
        this.buf = buf;
    }

    @Override
    public int read() throws IOException {
        if( buf==null ) {
            return -1;
        }
        if( !buf.hasRemaining() ) {
            return -1;
        }
        return buf.get() & 0xFF;
    }

    @Override
    public int read(byte[] bytes, int off, int len)
            throws IOException {
        if( buf==null ) {
            return -1;
        }
        if( !buf.hasRemaining() ) {
            return -1;
        }

        len = Math.min(len, buf.remaining());
        buf.get(bytes, off, len);
        return len;
    }
}
