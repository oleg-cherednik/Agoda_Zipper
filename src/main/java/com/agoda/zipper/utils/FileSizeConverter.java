package com.agoda.zipper.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Oleg Cherednik
 * @since 20.02.2019
 */
public final class FileSizeConverter {

    public static final int KB = 1024;
    public static final int MB = KB * 1024;

    private static final Pattern PATTERN = Pattern.compile("(?i)^(?<size>\\d+)\\s*(?<unit>(?:MB|GB))?$");

    public long convertToBytes(String str) {
        Matcher matcher = PATTERN.matcher(str);

        if (matcher.matches()) {
            int size = Integer.parseInt(matcher.group("size"));
            String unit = matcher.group("unit");
            unit = unit == null ? "MB" : unit;

            // TODO check size >= 1

            if ("KB".equalsIgnoreCase(unit))
                return size * KB;
            if ("MB".equalsIgnoreCase(unit))
                return size * MB;
        }

        throw new IllegalArgumentException("Argument <SPLIT_LENGTH>");
    }
}
