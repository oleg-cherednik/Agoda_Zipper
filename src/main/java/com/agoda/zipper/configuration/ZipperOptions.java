package com.agoda.zipper.configuration;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;

/**
 * @author Oleg Cherednik
 * @since 17.02.2019
 */
public class ZipperOptions extends Options {
    private static final long serialVersionUID = -4465819367987648574L;

    public ZipperOptions() {
        addOptionGroup(createCommandGroup());
        addOptionGroup(createHelpGroup());
    }

    private static OptionGroup createCommandGroup() {
        OptionGroup group = new OptionGroup();
        group.addOption(AddOption.INSTANCE);
        group.addOption(ExtractOption.INSTANCE);
        return group;
    }

    private static OptionGroup createHelpGroup() {
        OptionGroup group = new OptionGroup();
        group.addOption(HelpOption.INSTANCE);
        group.addOption(VersionOption.INSTANCE);
        return group;
    }

    public void parse(String... args) throws Exception {
        CommandLine cmd = new DefaultParser().parse(this, args);
        Option[] options = cmd.getOptions();

        if (options.length == 0 || cmd.hasOption(HelpOption.OPT))
            HelpOption.INSTANCE.execute(this);
        else if (cmd.hasOption(VersionOption.OPT))
            VersionOption.INSTANCE.execute(this);
        else if (cmd.hasOption(AddOption.OPT))
            AddOption.INSTANCE.execute(cmd);
        else if (cmd.hasOption(ExtractOption.OPT))
            ExtractOption.INSTANCE.execute(cmd);
    }

}
