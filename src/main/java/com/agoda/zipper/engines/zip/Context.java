package com.agoda.zipper.engines.zip;

import com.agoda.zipper.utils.FileSizeConverter;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FilenameUtils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Oleg Cherednik
 * @since 18.02.2019
 */
@Getter
@Setter
public class Context {

    public static final int NO_SPLIT = -1;
    public static final int MIN_SPLIT_LENGTH = FileSizeConverter.KB;

    private final List<ZipFile.Header> headers = new ArrayList<>();

    private int noOfThisDisk;
    private long offsCentralDirectory;
    private int noOfThisDiskStartOfCentralDir;
    private int totalEntries;

    private boolean splitArchive;
    private long splitLength = NO_SPLIT;
    private Path zipFile;
    private int splitCount;

    public void setNoOfThisDisk(int noOfThisDisk) {
        this.noOfThisDisk = noOfThisDisk;
        splitArchive = noOfThisDisk > 0;
    }

    public void addHeader(ZipFile.Header header) {
        if (header != null)
            headers.add(header);
    }

    public List<ZipFile.Header> getHeaders() {
        return Collections.unmodifiableList(headers);
    }

    void setSplitLength(long splitLength) {
        if (splitLength != NO_SPLIT && splitLength < MIN_SPLIT_LENGTH)
            throw new IllegalArgumentException("'splitLength' length should be either '-1' or greater than '" + MIN_SPLIT_LENGTH + "' bytes");

        this.splitLength = splitLength;
        splitArchive = splitLength > 0;
    }

    public void incSplitCount() {
        splitCount++;
    }

    public Path getSplitFilePath(int count) {
        return zipFile.getParent().resolve(String.format("%s.z%02d", FilenameUtils.getBaseName(zipFile.toString()), count));
    }

    public boolean shouldCreateNewSplitFile(long totalBytes) {
        return splitArchive && totalBytes > splitLength;
    }

    public boolean shouldWriteSplitSegment() {
        return splitArchive && headers.isEmpty();
    }
}
