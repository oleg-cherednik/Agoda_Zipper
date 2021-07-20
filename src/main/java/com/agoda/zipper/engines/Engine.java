package com.agoda.zipper.engines;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 22.02.2019
 */
public interface Engine {
    void compress(Path srcDir, Path destDir, long splitLength) throws IOException;

    void decompress(Path srcFile, Path destDir) throws IOException;
}
