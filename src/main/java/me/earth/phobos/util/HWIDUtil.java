package me.earth.phobos.util;

public class HWIDUtil extends RuntimeException { //NoStackTrace when crashes
    public HWIDUtil(String msg) {
        super(msg);
        setStackTrace(new StackTraceElement[0]);
    }




    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}