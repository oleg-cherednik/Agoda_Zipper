package com.agoda.zipper.engines.zip;

import com.agoda.zipper.engines.zip.io.ZipOutputStream;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Oleg Cherednik
 * @since 18.02.2019
 */
@Slf4j
@RequiredArgsConstructor
final class ZipApi {

    private final Context context;

    void createFromDirectory(Path srcDir) throws IOException {
        try (ZipOutputStream zip = new ZipOutputStream(context)) {
            for (Path path : getDirectoryEntries(srcDir)) {
                zip.putNextEntry(path, srcDir.getParent());

                if (Files.isRegularFile(path))
                    copyFile(path, zip);

                zip.closeEntry();
            }

            zip.finish();
        }
    }

    @NonNull
    private static List<Path> getDirectoryEntries(@NonNull Path dir) {
        try {
            return Files.walk(dir)
                        .filter(path -> Files.isRegularFile(path) || Files.isDirectory(path))
                        .collect(Collectors.toList());
        } catch(IOException e) {
            log.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private static void copyFile(@NonNull Path path, @NonNull OutputStream out) throws IOException {
        try (InputStream in = new FileInputStream(path.toFile())) {
            IOUtils.copyLarge(in, out);
        }
    }

}
