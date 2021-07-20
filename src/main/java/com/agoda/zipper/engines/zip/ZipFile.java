package com.agoda.zipper.engines.zip;

import com.agoda.zipper.engines.zip.io.HeaderReader;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 18.02.2019
 */
@AllArgsConstructor
public final class ZipFile {

    @NonNull
    private Path zipFile;

    public void zipIt(Path src, long splitLength) throws IOException {
        if (Files.isDirectory(zipFile))
            zipFile = zipFile.resolve(src.getFileName() + ".zip");

        if (Files.isDirectory(src))
            zipDirectory(src, splitLength);
        else
            zipRegularFile(src, splitLength);
    }

    private void zipDirectory(Path srcDir, long splitLength) throws IOException {
        Context context = new Context();
        context.setZipFile(zipFile);
        context.setSplitLength(splitLength);

        new ZipApi(context).createFromDirectory(srcDir);
    }

    private static void zipRegularFile(Path srcFile, long splitLength) {
        throw new NotImplementedException("Create zip from regular file is not supported");
    }

    public void unzipIt(Path destDir) throws IOException {
        if (Files.isRegularFile(destDir))
            throw new IllegalArgumentException("'destDir' should be directory");

        Context context = createContext();

        for (ZipFile.Header header : context.getHeaders()) {
            boolean directory = header.isDirectory();
            Path parentDir = destDir.resolve(header.getFileName());

            if (!directory)
                parentDir = parentDir.getParent();

            if (!Files.exists(parentDir))
                Files.createDirectories(parentDir);

            if (!directory)
                new UnzipApi(context, header).unzipFile(destDir);
        }
    }

    private Context createContext() throws IOException {
        try (RandomAccessFile file = new RandomAccessFile(zipFile.toFile(), "r")) {
            Context context = new HeaderReader(file).createContext();
            context.setZipFile(zipFile);
            return context;
        }
    }

    @Getter
    @Setter
    public static final class Header {

        private long compressedSize;
        private long uncompressedSize;
        private int fileNameLength;
        private int splitCount;
        private long offsLocalHeader;
        private String fileName;
        private long offsData;
        private long crc32;

        public void setFileName(String fileName) {
            this.fileName = fileName;
            fileNameLength = (short)StringUtils.length(fileName);
        }

        public boolean isDirectory() {
            return fileNameLength > 0 && (fileName.endsWith("/") || fileName.endsWith("\\"));
        }

        public void setUncompressedSize(long uncompressedSize) {
            this.uncompressedSize = isDirectory() ? 0 : uncompressedSize;
        }

        public Header updateZeroValue(Header header) {
            crc32 = crc32 > 0 ? crc32 : header.crc32;
            compressedSize = compressedSize > 0 ? compressedSize : header.compressedSize;
            uncompressedSize = uncompressedSize > 0 ? uncompressedSize : header.uncompressedSize;
            return this;
        }
    }
}
