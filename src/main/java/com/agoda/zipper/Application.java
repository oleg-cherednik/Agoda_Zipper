package com.agoda.zipper;

import com.agoda.zipper.configuration.ZipperOptions;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author Oleg Cherednik
 * @since 17.02.2019
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Application {
    public static void main(String... args) throws Exception {
        new ZipperOptions().parse(args);
    }
}
