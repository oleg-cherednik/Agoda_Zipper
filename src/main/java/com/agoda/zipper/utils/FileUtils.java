package com.agoda.zipper.utils;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Oleg Cherednik
 * @since 19.02.2019
 */
@Slf4j
@UtilityClass
public class FileUtils {
    @NonNull
    public static List<Path> getDirectoryEntries(@NonNull Path path) {
        try {
            return Files.walk(path).parallel()
                        .filter(path1 -> Files.isRegularFile(path1) || Files.isDirectory(path1))
                        .collect(Collectors.toList());
        } catch(IOException e) {
            log.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}
