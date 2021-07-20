package com.agoda.zipper.engines.zip.io;

import com.agoda.zipper.engines.zip.ZipFile;
import com.agoda.zipper.utils.LittleEndianBuffer;
import com.agoda.zipper.engines.zip.Constants;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Oleg Cherednik
 * @since 20.02.2019
 */
@RequiredArgsConstructor
final class HeaderWriter {

    private final ZipFile.Header header;

    int writeLocSig(OutputStream out) throws IOException {
        try (LittleEndianBuffer buf = new LittleEndianBuffer()) {
            buf.writeInt(Constants.LOCSIG);
            buf.writeShort((short)20);
            buf.writeBytes((byte)8, (byte)0);
            buf.writeShort(Constants.COMP_DEFLATE);
            buf.writeInt((int)System.currentTimeMillis());
            buf.writeInt((int)header.getCrc32());
            buf.writeInt((short)header.getCompressedSize());
            buf.writeInt((short)header.getUncompressedSize());
            buf.writeShort((short)header.getFileNameLength());
            buf.writeShort((short)0);
            buf.writeString(header.getFileName());
            return buf.flushInto(out);
        }
    }

    int writeExtSig(OutputStream out) throws IOException {
        try (LittleEndianBuffer buf = new LittleEndianBuffer()) {
            buf.writeInt(Constants.EXTSIG);
            buf.writeInt((int)header.getCrc32());
            buf.writeInt((int)header.getCompressedSize());
            buf.writeInt((int)header.getUncompressedSize());
            return buf.flushInto(out);
        }
    }

    void writeCenSig(LittleEndianBuffer buf) {
        buf.writeInt(Constants.CENSIG);
        buf.writeShort((short)20);
        buf.writeShort((short)20);
        buf.writeBytes((byte)8, (byte)0);
        buf.writeShort(Constants.COMP_DEFLATE);
        buf.writeInt(System.currentTimeMillis());
        buf.writeInt(header.getCrc32());
        buf.writeInt(header.getCompressedSize());
        buf.writeInt(header.getUncompressedSize());
        buf.writeShort((short)header.getFileNameLength());
        buf.writeShort((short)0);
        buf.writeShort((short)0);
        buf.writeShort((short)header.getSplitCount());
        buf.writeShort((short)0);
        buf.writeInt(0);
        buf.writeInt(header.getOffsLocalHeader());
        buf.writeString(header.getFileName());
    }

}
