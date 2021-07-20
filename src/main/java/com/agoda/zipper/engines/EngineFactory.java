package com.agoda.zipper.engines;

import lombok.RequiredArgsConstructor;
import org.apache.commons.cli.CommandLine;

import java.util.function.Supplier;

/**
 * @author Oleg Cherednik
 * @since 22.02.2019
 */
@RequiredArgsConstructor
public final class EngineFactory implements Supplier<Engine> {
    private final CommandLine cmd;

    @Override
    public Engine get() {
        return ZipEngine.INSTANCE;
    }
}
