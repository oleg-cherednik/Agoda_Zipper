package com.agoda.zipper.engines.zip.io;

import com.agoda.zipper.engines.zip.Constants;
import com.agoda.zipper.engines.zip.Context;
import com.agoda.zipper.engines.zip.ZipFile;
import com.agoda.zipper.utils.LittleEndianWrapper;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * @author Oleg Cherednik
 * @since 20.02.2019
 */
public final class HeaderReader {

    private final RandomAccessFile file;
    private final LittleEndianWrapper in;

    public HeaderReader(RandomAccessFile file) {
        this.file = file;
        in = new LittleEndianWrapper(this.file);
    }

    public Context createContext() throws IOException {
        Context context = new Context();
        readEndSig(context);
        readHeaders(context);
        return context;
    }

    private void readEndSig(Context context) throws IOException {
        findHeader();

        context.setNoOfThisDisk(in.readShort());
        in.readShort();
        in.readShort();
        context.setTotalEntries(in.readShort());
        in.readInt();
        context.setOffsCentralDirectory(in.readIntAsLong());
        in.readShort();
    }

    private void findHeader() throws IOException {
        long pos = file.length() - Constants.ENDHDR;

        for (int i = 0; i < 1000; i++) {
            file.seek(pos--);

            if (in.readInt() == Constants.ENDSIG)
                return;
        }

        throw new IOException("No zip file header found");
    }

    private void readHeaders(Context context) throws IOException {
        file.seek(context.getOffsCentralDirectory());

        for (int i = 0; i < context.getTotalEntries(); i++)
            context.addHeader(readCenSig());
    }

    private ZipFile.Header readCenSig() throws IOException {
        ZipFile.Header header = new ZipFile.Header();

        if (in.readInt() != Constants.CENSIG)
            throw new IOException("Incorrect CENSIG signature");

        in.readShort();     // create version
        in.readShort();     // extract version
        in.readShort();     // bit flags
        in.readShort();     // compression
        in.readIntAsLong(); // last modified date
        header.setCrc32(in.readIntAsLong());
        header.setCompressedSize(in.readIntAsLong());
        header.setUncompressedSize(in.readIntAsLong());
        header.setFileNameLength(in.readShort());
        in.readShort();     // extra field size

        int commentLength = in.readShort();

        header.setSplitCount(in.readShort());
        in.readShort();     // internal attributes
        in.readInt();       // external attributes
        header.setOffsLocalHeader(in.readIntAsLong());
        header.setFileName(in.readString(header.getFileNameLength()));

        in.readString(commentLength);

        return header;
    }

    public ZipFile.Header readLocalHeader(long offsLocalHeader) throws IOException {
        file.seek(offsLocalHeader);
        in.resetOffs();

        ZipFile.Header header = new ZipFile.Header();

        if (in.readInt() != Constants.LOCSIG)
            throw new IOException("Incorrect LOCSIG signature");

        in.readShort();     // extract version
        in.readShort();     // bit flags
        in.readShort();     // compression
        in.readIntAsLong(); // last modified date
        header.setCrc32(in.readIntAsLong());
        header.setCompressedSize(in.readIntAsLong());
        header.setUncompressedSize(in.readIntAsLong());
        header.setFileNameLength(in.readShort());
        in.readShort();     // extra field size

        header.setFileName(in.readString(header.getFileNameLength()));
        header.setOffsData(offsLocalHeader + in.getOffs());

        return header;
    }
}
