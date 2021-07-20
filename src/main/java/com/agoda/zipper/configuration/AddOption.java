package com.agoda.zipper.configuration;

import com.agoda.zipper.engines.EngineFactory;
import com.agoda.zipper.utils.FileSizeConverter;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Oleg Cherednik
 * @since 18.02.2019
 */
final class AddOption extends Option {
    private static final long serialVersionUID = -8146785622074449555L;

    private static final int ARG_SRC_DIR = 0;
    private static final int ARG_DEST_DIR = 1;
    private static final int ARG_SPLIT_LENGTH = 2;

    static final AddOption INSTANCE = new AddOption();
    static final String OPT = "a";

    private AddOption() {
        super(OPT, "add", true, "add directory to archive");
        setArgName("SRC_DIR DEST_DIR SPLIT_LENGTH");
        setOptionalArg(true);
        setArgs(3);
    }

    public void execute(CommandLine cmd) throws IOException {
        String[] args = cmd.getOptionValues(OPT);

        Path srcDir = Paths.get(args[ARG_SRC_DIR]);
        Path destDir = Paths.get(args[ARG_DEST_DIR]);
        long splitLength = args.length > ARG_SPLIT_LENGTH ? new FileSizeConverter().convertToBytes(args[ARG_SPLIT_LENGTH]) : -1;

        new EngineFactory(cmd).get().compress(srcDir, destDir, splitLength);
    }

}
