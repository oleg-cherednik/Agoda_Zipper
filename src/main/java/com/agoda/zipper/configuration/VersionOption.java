package com.agoda.zipper.configuration;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

/**
 * @author Oleg Cherednik
 * @since 17.02.2019
 */
final class VersionOption extends Option {

    private static final long serialVersionUID = -1122910245943210658L;
    public static final VersionOption INSTANCE = new VersionOption();

    public static final String OPT = "v";

    private VersionOption() {
        super(OPT, "version", false, "print product version and exit");
    }

    public void execute(Options options) {
        System.out.println("product version: \"Zipper v1.0\"");
        System.out.println("java version: \"" + System.getProperty("java.runtime.version") + '"');
    }
}
