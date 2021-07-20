package com.agoda.zipper.engines.zip.io;

import com.agoda.zipper.engines.zip.Constants;
import com.agoda.zipper.engines.zip.UnzipApi;

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * @author Oleg Cherednik
 * @since 20.02.2019
 */
public class ZipInputStream extends SplitInputStream {

    private final Inflater inflater = new Inflater(true);
    private final long uncompressedSize;
    private final byte[] buf = new byte[Constants.BUF_SIZE];
    private long bytesWritten;

    public ZipInputStream(RandomAccessFile file, UnzipApi api) throws IOException {
        super(file, api);
        uncompressedSize = api.getHeader().getUncompressedSize();
    }

    @Override
    public int read() throws IOException {
        byte[] buf = new byte[1];
        return read(buf) == -1 ? -1 : buf[0] & 0xff;
    }

    @Override
    public int read(byte[] buf) throws IOException {
        return read(buf, 0, buf.length);
    }

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
        try {
            if (bytesWritten >= uncompressedSize) {
                finishInflating();
                return -1;
            }

            int count;

            while ((count = inflater.inflate(buf, offs, len)) == 0) {
                if (inflater.finished() || inflater.needsDictionary()) {
                    finishInflating();
                    return -1;
                }

                if (inflater.needsInput())
                    fill();
            }

            bytesWritten += count;
            return count;
        } catch(DataFormatException e) {
            throw new IOException(e);
        }
    }

    private void finishInflating() throws IOException {
        byte[] buf = new byte[1024];
        while (super.read(buf, 0, 1024) != -1) {
        }
    }

    private void fill() throws IOException {
        int len = super.read(buf, 0, buf.length);

        if (len == -1)
            throw new EOFException("Unexpected end of ZLIB input stream");

        inflater.setInput(buf, 0, len);
    }

    @Override
    public long skip(long n) throws IOException {
        int max = (int)Math.min(n, Integer.MAX_VALUE);
        int total = 0;
        byte[] buf = new byte[512];

        while (total < max)
            total += Math.max(0, read(buf, 0, Math.min(buf.length, max - total)));

        return total;
    }


    @Override
    public int available() {
        return inflater.finished() ? 0 : 1;
    }

    @Override
    public void close() throws IOException {
        inflater.end();
        super.close();
    }
}
