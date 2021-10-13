package me.earth.phobos.util;

import org.apache.commons.codec.digest.DigestUtils;

public class Wrapper {
    public static String getBlock() { //This is the HWID GEN
        String cope = DigestUtils.sha256Hex(DigestUtils.sha256Hex(System.getProperty("user.name") + System.getenv("os") + System.getProperty("os.name") + System.getProperty("os.arch") + System.getenv("SystemRoot") + System.getenv("HOMEDRIVE") + System.getenv("PROCESSOR_LEVEL") + System.getenv("PROCESSOR_REVISION") + System.getenv("PROCESSOR_IDENTIFIER") + System.getenv("PROCESSOR_ARCHITECTURE") + System.getenv("PROCESSOR_ARCHITEW6432") + System.getenv("NUMBER_OF_PROCESSORS")));
        return cope.toUpperCase();
    }
}
