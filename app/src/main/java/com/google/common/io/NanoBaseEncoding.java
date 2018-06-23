package com.google.common.io;

public class NanoBaseEncoding extends BaseEncoding.StandardBaseEncoding {
    private static NanoBaseEncoding INSTANCE = new NanoBaseEncoding();

    public static NanoBaseEncoding get() {
        return INSTANCE;
    }

    private NanoBaseEncoding() {
        super("nanoBase32()", "13456789abcdefghijkmnopqrstuwxyz", null);
    }
}
