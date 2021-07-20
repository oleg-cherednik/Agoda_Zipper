package com.agoda.zipper.engines.zip.io;

import com.agoda.zipper.engines.zip.ZipFile;
import com.agoda.zipper.engines.zip.UnzipApi;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

/**
 * @author Oleg Cherednik
 * @since 20.02.2019
 */
public class SplitInputStream extends InputStream {
    private final long compressedSize;
    private final UnzipApi api;

    private RandomAccessFile file;
    private long totalBytesRead;

    public SplitInputStream(RandomAccessFile file, UnzipApi api) throws IOException {
        this.file = file;
        this.api = api;
        compressedSize = api.getHeader().getCompressedSize();

        file.seek(getOffsData(file, api.getHeader()));
    }

    private static long getOffsData(RandomAccessFile file, ZipFile.Header header) throws IOException {
        return new HeaderReader(file).readLocalHeader(header.getOffsLocalHeader()).updateZeroValue(header).getOffsData();
    }

    @Override
    public int available() {
        return (int)Math.min(compressedSize - totalBytesRead, Integer.MAX_VALUE);
    }

    @Override
    public int read() throws IOException {
        if (totalBytesRead >= compressedSize)
            return -1;

        byte[] buf = new byte[1];
        return read(buf) == -1 ? -1 : buf[0] & 0xFF;
    }

    @Override
    public int read(byte[] buf) throws IOException {
        return read(buf, 0, buf.length);
    }

    @Override
    public int read(byte[] buf, int off, int len) throws IOException {
        if (len > compressedSize - totalBytesRead) {
            len = (int)(compressedSize - totalBytesRead);

            if (len == 0)
                return -1;
        }

        int count = file.read(buf, off, len);

        if ((count < len) && api.getContext().isSplitArchive()) {
            file.close();
            file = api.createNextSplitFile();
            count = Math.max(0, count);
            count += Math.max(0, file.read(buf, count, len - count));
        }

        totalBytesRead += Math.max(0, count);

        return count;
    }

    @Override
    public long skip(long amount) throws IOException {
        if (amount < 0)
            throw new IllegalArgumentException();
        if (amount > compressedSize - totalBytesRead)
            amount = compressedSize - totalBytesRead;
        totalBytesRead += amount;
        return amount;
    }

    @Override
    public void close() throws IOException {
        if (file != null)
            file.close();
    }
}
