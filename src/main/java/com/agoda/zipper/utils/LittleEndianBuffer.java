package com.agoda.zipper.utils;

import lombok.Getter;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * @author Oleg Cherednik
 * @since 21.02.2019
 */
public final class LittleEndianBuffer implements Closeable {
    private static final int DEF_SIZE = 30;

    private byte[] buf;
    @Getter
    private int offs;

    public LittleEndianBuffer() {
        this(DEF_SIZE);
    }

    @SuppressWarnings("WeakerAccess")
    public LittleEndianBuffer(int size) {
        buf = new byte[size];
        Arrays.fill(buf, (byte)-1);
    }

    public LittleEndianBuffer writeInt(long val) {
        ensureEnoughSpace(4);
        buf[offs++] = (byte)(val & 0xFF);
        buf[offs++] = (byte)(val >>> 8);
        buf[offs++] = (byte)(val >>> 16);
        buf[offs++] = (byte)(val >>> 24);
        return this;
    }

    public LittleEndianBuffer writeShort(short val) {
        ensureEnoughSpace(2);
        buf[offs++] = (byte)(val & 0xFF);
        buf[offs++] = (byte)(val >>> 8);
        return this;
    }

    public LittleEndianBuffer writeString(String str) {
        return str != null ? writeBytes(str.getBytes()) : this;
    }

    public LittleEndianBuffer writeBytes(byte... arr) {
        ensureEnoughSpace(arr.length);

        for (byte val : arr)
            buf[offs++] = (byte)(val & 0xFF);

        return this;
    }

    public int flushInto(OutputStream out) throws IOException {
        out.write(buf, 0, offs);
        return offs;
    }

    private void ensureEnoughSpace(int bytesToWrite) {
        if (offs + bytesToWrite > buf.length) {
            byte[] arr = new byte[(offs + bytesToWrite) * 2];
            Arrays.fill(arr, (byte)-1);
            System.arraycopy(buf, 0, arr, 0, offs);
            buf = arr;
        }
    }

    @Override
    public void close() throws IOException {
        buf = null;
    }

    @Override
    public String toString() {
        return "size: " + offs;
    }

}
