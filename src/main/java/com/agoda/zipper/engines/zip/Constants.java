package com.agoda.zipper.engines.zip;

import lombok.experimental.UtilityClass;

/**
 * @author Oleg Cherednik
 * @since 20.02.2019
 */
@UtilityClass
public class Constants {

    // java.util.zip.ZipConstants

    /* Header signatures */
    public static final int LOCSIG = 0x04034b50;   // "PK\003\004"
    public static final int EXTSIG = 0x08074b50;   // "PK\007\008"
    public static final int CENSIG = 0x02014b50;   // "PK\001\002"
    public static final int ENDSIG = 0x06054b50;   // "PK\005\006"
    public static final int SPLITSIG = 0x08074b50;

    private static final int DIGSIG = 0x05054b50;
    private static final int ARCEXTDATREC = 0x08064b50;
    private static final int ZIP64ENDCENDIRLOC = 0x07064b50;
    private static final int ZIP64ENDCENDIRREC = 0x06064b50;
    private static final int EXTRAFIELDZIP64LENGTH = 0x0001;
    private static final int AESSIG = 0x9901;

    /* Header sizes in bytes (including signatures) */
    public static final int ENDHDR = 22;    // END header size

    public static final short COMP_DEFLATE = 8;

    public static final int BUF_SIZE = 1024;

    public static int[] getHeadSignatures() {
        return new int[] { LOCSIG, EXTSIG, CENSIG, ENDSIG, DIGSIG, ARCEXTDATREC, SPLITSIG,
                ZIP64ENDCENDIRLOC, ZIP64ENDCENDIRREC, EXTRAFIELDZIP64LENGTH, AESSIG };
    }

}
