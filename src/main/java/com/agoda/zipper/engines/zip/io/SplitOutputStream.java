package com.agoda.zipper.engines.zip.io;

import com.agoda.zipper.engines.zip.Constants;
import com.agoda.zipper.engines.zip.Context;
import com.agoda.zipper.utils.LittleEndianWrapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.ArrayUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 20.02.2019
 */
@RequiredArgsConstructor
final class SplitOutputStream extends OutputStream {
    private final Context context;

    private RandomAccessFile file;
    private long bytesWritten;

    private void createFile() throws IOException {
        if (file != null)
            file.close();

        file = new RandomAccessFile(context.getZipFile().toFile(), "rw");
        bytesWritten = 0;
    }

    public int writeInt(long val) throws IOException {
        write((int)(val & 0xFF));
        write((int)((val >>> 8) & 0xFF));
        write((int)((val >>> 16) & 0xFF));
        write((int)((val >>> 24) & 0xFF));
        return 4;
    }

    @Override
    public void write(int buf) throws IOException {
        byte[] buff = new byte[1];
        buff[0] = (byte)buf;
        write(buff, 0, 1);
    }

    @Override
    public void write(byte[] buf) throws IOException {
        write(buf, 0, buf.length);
    }

    @Override
    public void write(byte[] buf, int off, int len) throws IOException {
        if (len <= 0)
            return;

        if (file == null)
            createFile();

        if (context.getSplitLength() == Context.NO_SPLIT || bytesWritten + len <= context.getSplitLength())
            addToFile(buf, off, len);
        else if (bytesWritten >= context.getSplitLength() || isHeader(buf)) {
            createNextSplitFile();
            addToFile(buf, off, len);
        } else {
            int delta = (int)(context.getSplitLength() - bytesWritten);
            addToFile(buf, off, delta);
            createNextSplitFile();
            addToFile(buf, off + delta, len - delta);
        }
    }

    private void addToFile(byte[] buf, int off, int len) throws IOException {
        file.write(buf, off, len);
        bytesWritten += len;
    }

    private void createNextSplitFile() throws IOException {
        Path file = context.getSplitFilePath(context.getSplitCount() + 1);
        this.file.close();

        if (Files.exists(file))
            throw new IOException("Split file '" + file.getFileName() + "' exists");

        Files.move(context.getZipFile(), context.getZipFile().resolveSibling(file));
        createFile();
        context.incSplitCount();
    }

    private static boolean isHeader(byte... buf) {
        if (ArrayUtils.getLength(buf) < 4)
            return false;

        int signature = LittleEndianWrapper.readInt(buf);

        for (long headerSignature : Constants.getHeadSignatures())
            if (headerSignature != Constants.SPLITSIG && headerSignature == signature)
                return true;

        return false;
    }

    boolean createNextSplitFileIfRequired(int bytesToWrite) throws IOException {
        if (!context.shouldCreateNewSplitFile(bytesWritten + bytesToWrite))
            return false;

        createNextSplitFile();
        return true;
    }

    @Override
    public void close() throws IOException {
        file.close();
    }

    long getFilePointer() throws IOException {
        return file.getFilePointer();
    }
}
