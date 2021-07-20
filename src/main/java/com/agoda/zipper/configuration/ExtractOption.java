package com.agoda.zipper.configuration;

import com.agoda.zipper.engines.EngineFactory;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Oleg Cherednik
 * @since 18.02.2019
 */
final class ExtractOption extends Option {

    private static final long serialVersionUID = -3881473894927722873L;

    private static final int ARG_SRC_FILE = 0;
    private static final int ARG_DEST_DIR = 1;

    static final ExtractOption INSTANCE = new ExtractOption();
    static final String OPT = "x";

    private ExtractOption() {
        super(OPT, "extract", true, "extract file with full path");
        setArgName("SRC_FILE DEST_DIR");
        setArgs(2);
    }

    public void execute(CommandLine cmd) throws Exception {
        String[] args = cmd.getOptionValues(OPT);

        Path srcFile = Paths.get(args[ARG_SRC_FILE]);
        Path destDir = Paths.get(args[ARG_DEST_DIR]);

        new EngineFactory(cmd).get().decompress(srcFile, destDir);
    }
}
