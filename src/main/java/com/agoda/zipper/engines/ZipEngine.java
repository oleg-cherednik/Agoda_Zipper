package com.agoda.zipper.engines;

import com.agoda.zipper.engines.zip.ZipFile;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 22.02.2019
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class ZipEngine implements Engine {

    public static final ZipEngine INSTANCE = new ZipEngine();

    @Override
    public void compress(Path srcDir, Path destDir, long splitLength) throws IOException {
        new ZipFile(destDir).zipIt(srcDir, splitLength);
    }

    @Override
    public void decompress(Path srcFile, Path destDir) throws IOException {
        new ZipFile(srcFile).unzipIt(destDir);
    }
}
