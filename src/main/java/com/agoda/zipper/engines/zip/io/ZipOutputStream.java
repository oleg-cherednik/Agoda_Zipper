package com.agoda.zipper.engines.zip.io;

import com.agoda.zipper.engines.zip.Constants;
import com.agoda.zipper.engines.zip.ZipFile;
import com.agoda.zipper.utils.LittleEndianBuffer;
import com.agoda.zipper.engines.zip.Context;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.CRC32;
import java.util.zip.Deflater;

/**
 * @author Oleg Cherednik
 * @since 17.02.2019
 */
public class ZipOutputStream extends OutputStream {

    private final SplitOutputStream out;
    private final Context context;
    private final Deflater def = new Deflater();
    private final CRC32 crc = new CRC32();

    private Path entryPath;
    private ZipFile.Header header;
    private Path defaultFolderPath;
    private long entryBytesWritten;

    private long totalBytesWritten;

    public ZipOutputStream(Context context) {
        this.context = context;
        out = new SplitOutputStream(context);
    }

    public void putNextEntry(Path entryPath, Path defaultFolderPath) throws IOException {
        this.entryPath = entryPath;
        this.defaultFolderPath = defaultFolderPath;

        header = createHeader();
        totalBytesWritten += context.shouldWriteSplitSegment() ? out.writeInt(Constants.SPLITSIG) : 0;
        header.setOffsLocalHeader(context.isSplitArchive() && totalBytesWritten != 4 ? out.getFilePointer() : totalBytesWritten);
        totalBytesWritten += new HeaderWriter(header).writeLocSig(out);

        crc.reset();
        def.reset();
    }

    private ZipFile.Header createHeader() throws IOException {
        ZipFile.Header header = new ZipFile.Header();

        header.setFileName(defaultFolderPath.relativize(entryPath).toString() + (Files.isDirectory(entryPath) ? "/" : ""));
        header.setSplitCount(context.getSplitCount());
        header.setUncompressedSize(header.isDirectory() ? 0 : Files.size(entryPath));

        return header;
    }

    @Override
    public void write(int buf) throws IOException {
        byte[] buff = new byte[1];
        buff[0] = (byte)buf;
        write(buff, 0, 1);
    }

    @Override
    public void write(byte[] buf, int offs, int len) throws IOException {
        crc.update(buf, offs, len);
        def.setInput(buf, offs, len);

        while (!def.needsInput())
            deflate();
    }

    private void writeBuf(byte[] buf, int offs, int len) throws IOException {
        if (len <= 0)
            return;

        out.write(buf, offs, len);
        totalBytesWritten += len;
        entryBytesWritten += len;
    }

    private final byte[] buf = new byte[Constants.BUF_SIZE];
    private boolean firstBytes;

    private void deflate() throws IOException {
        int len = def.deflate(buf, 0, buf.length);

        if (len <= 0)
            return;

        if (def.finished()) {
            if (len < 4) {
                if (4 - len <= entryBytesWritten)
                    entryBytesWritten -= 4 - len;
            } else if (len <= 4)
                return;
            else
                len -= 4;
        }

        writeBuf(buf, firstBytes ? 0 : 2, firstBytes ? len : len - 2);
        firstBytes = true;
    }

    public void closeEntry() throws IOException {
        if (!def.finished()) {
            def.finish();

            while (!def.finished())
                deflate();
        }

        firstBytes = false;
        header.setCompressedSize(entryBytesWritten);
        header.setCrc32(crc.getValue());
        context.addHeader(header);
        totalBytesWritten += new HeaderWriter(header).writeExtSig(out);
        crc.reset();
        entryBytesWritten = 0;
    }

    public void finish() throws IOException {
        context.setOffsCentralDirectory(totalBytesWritten);
        processHeaderData();

        try (LittleEndianBuffer buf = new LittleEndianBuffer()) {
            context.getHeaders().forEach(header -> new HeaderWriter(header).writeCenSig(buf));

            int sizeCentralDirectory = buf.getOffs();

            buf.writeInt(Constants.ENDSIG);
            buf.writeShort((short)context.getNoOfThisDisk());
            buf.writeShort((short)context.getNoOfThisDiskStartOfCentralDir());

            int numEntriesOnThisDisk = context.getHeaders().size();

            if (context.isSplitArchive())
                numEntriesOnThisDisk = (int)context.getHeaders().stream()
                                                   .filter(header -> header.getSplitCount() == context.getNoOfThisDisk())
                                                   .count();

            buf.writeShort((short)numEntriesOnThisDisk);
            buf.writeShort((short)context.getHeaders().size());
            buf.writeInt(sizeCentralDirectory);
            buf.writeInt(context.getOffsCentralDirectory());
            buf.writeShort((short)0);

            writeZipHeaderBytes(buf);
        }
    }

    private void writeZipHeaderBytes(LittleEndianBuffer buf) throws IOException {
        if (out.createNextSplitFileIfRequired(buf.getOffs()))
            finish();
        else
            buf.flushInto(out);
    }

    private void processHeaderData() throws IOException {
        int currSplitFileCounter = 0;

        if (context.isSplitArchive()) {
            context.setOffsCentralDirectory(out.getFilePointer());
            currSplitFileCounter = context.getSplitCount();
        }

        context.setNoOfThisDisk(currSplitFileCounter);
        context.setNoOfThisDiskStartOfCentralDir(currSplitFileCounter);
    }

    @Override
    public void close() throws IOException {
        out.close();
    }

}
