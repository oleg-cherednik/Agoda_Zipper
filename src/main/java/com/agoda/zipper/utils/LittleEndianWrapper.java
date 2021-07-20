package com.agoda.zipper.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.DataInput;
import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 21.02.2019
 */
@RequiredArgsConstructor
public final class LittleEndianWrapper /*implements DataInput*/ {
    private final DataInput in;
    @Getter
    private int offs = 0;

    public void resetOffs() {
        offs = 0;
    }

    public short readShort() throws IOException {
        offs += 2;
        return convertShort(in.readShort());
    }

    public int readInt() throws IOException {
        offs += 4;
        return (int)convertInt(in.readInt());
    }

    public String readString(int length) throws IOException {
        if (length <= 0)
            return null;

        offs += length;
        byte[] buf = new byte[length];
        in.readFully(buf);
        return new String(buf);
    }

    public long readIntAsLong() throws IOException {
        offs += 4;
        return convertInt(in.readInt());
    }

    private static short convertShort(short val) {
        return (short)(getByte(val, 0) << 8 | getByte(val, 1));
    }

    private static long convertInt(int val) {
        return getByte(val, 0) << 24 | getByte(val, 1) << 16 | getByte(val, 2) << 8 | getByte(val, 3);
    }

    private static long getByte(long val, int i) {
        return (val >> i * 8) & 0xFF;
    }

    public static int readInt(byte[] buf) {
        return (buf[3] & 0xFF) << 24 | (buf[2] & 0xFF) << 16 | (buf[1] & 0xFF) << 8 | (buf[0] & 0xFF);
    }

}
