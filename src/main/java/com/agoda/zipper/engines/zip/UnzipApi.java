package com.agoda.zipper.engines.zip;

import com.agoda.zipper.engines.zip.io.ZipInputStream;
import com.agoda.zipper.utils.LittleEndianWrapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 18.02.2019
 */
@Getter
@RequiredArgsConstructor
public class UnzipApi {

    private final Context context;
    private final ZipFile.Header header;

    private int curSplitCount;

    public void unzipFile(Path destDir) throws IOException {
        try (InputStream in = new ZipInputStream(openFileForRead(), this);
             OutputStream out = getOutputStream(destDir)) {
            IOUtils.copyLarge(in, out);
        }
    }

    private RandomAccessFile openFileForRead() throws IOException {
        Path pathZip = context.getZipFile();

        if (context.isSplitArchive() && header.getSplitCount() != context.getNoOfThisDisk()) {
            curSplitCount = header.getSplitCount() + 1;
            pathZip = context.getSplitFilePath(curSplitCount);
        }

        RandomAccessFile file = new RandomAccessFile(pathZip.toFile(), "r");

        if (curSplitCount == 1)
            if (new LittleEndianWrapper(file).readInt() != Constants.SPLITSIG)
                throw new IOException("Incorrect SPLITSIG signature");

        return file;
    }

    private FileOutputStream getOutputStream(Path destDir) throws IOException {
        Path file = destDir.resolve(header.getFileName());

        if (!Files.exists(file.getParent()))
            Files.createDirectories(file.getParent());
        if (Files.exists(file))
            Files.delete(file);

        return new FileOutputStream(file.toFile());
    }

    public RandomAccessFile createNextSplitFile() throws IOException {
        Path file = context.getZipFile();

        if (curSplitCount != context.getNoOfThisDisk())
            file = context.getSplitFilePath(curSplitCount + 1);

        curSplitCount++;

        if (!Files.exists(file))
            throw new IOException("Split file '" + file.getFileName() + "' does not exist");

        return new RandomAccessFile(file.toFile(), "r");
    }

}

