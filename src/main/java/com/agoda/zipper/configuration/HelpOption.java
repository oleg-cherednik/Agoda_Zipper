package com.agoda.zipper.configuration;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.io.PrintWriter;

/**
 * @author Oleg Cherednik
 * @since 18.02.2019
 */
final class HelpOption extends Option {

    private static final long serialVersionUID = -3086653317249953008L;
    public static final HelpOption INSTANCE = new HelpOption();

    public static final String OPT = "?";

    private HelpOption() {
        super(OPT, "help", false, "print this help message");
    }

    public void execute(Options options) {
        try (PrintWriter out = new PrintWriter(System.out)) {
            new HelpFormatter().printHelp(out, 100, "java zipper.jar", null, options, 3, 5, null, true);
        }
    }
}
