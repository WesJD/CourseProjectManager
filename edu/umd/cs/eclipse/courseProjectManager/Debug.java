package edu.umd.cs.eclipse.courseProjectManager;

import java.util.*;
import java.text.*;
import java.io.*;

public class Debug
{
    private static final String OPTION_DEBUG = "edu.umd.cs.eclipse.courseProjectManager/debug";
    static final boolean DEBUG;
    static final boolean DEBUG_TIMING;
    
    static {
        if (!Boolean.getBoolean("edu.umd.cs.eclipse.courseProjectManager.debug")) {
            AutoCVSPlugin.getPlugin().isDebugging();
        }
        DEBUG = true;
        DEBUG_TIMING = Debug.DEBUG;
    }
    
    public static void print(final String msg) {
        if (!Debug.DEBUG) {
            return;
        }
        final StringBuffer msgBuf = new StringBuffer(msg.length() + 40);
        if (Debug.DEBUG_TIMING) {
            final DateFormat DEBUG_FORMAT = new SimpleDateFormat("HH:mm:ss.SSS");
            DEBUG_FORMAT.format(new Date(), msgBuf, new FieldPosition(0));
            msgBuf.append('-');
        }
        msgBuf.append('[').append(Thread.currentThread()).append(']').append(msg);
        System.out.println(msgBuf.toString());
    }
    
    public static void print(final String message, final Throwable e) {
        if (Debug.DEBUG) {
            print(message);
            if (e != null) {
                e.printStackTrace(System.out);
            }
        }
    }
    
    public static void printArray(final String name, final Object[] arr) {
        printArray(name, arr, System.out);
    }
    
    public static void printArray(final String name, final Object[] arr, final PrintStream stream) {
        stream.print(String.valueOf(name) + ": ");
        for (int ii = 0; ii < arr.length; ++ii) {
            stream.println(String.valueOf(name) + "[" + ii + "]: " + arr[ii]);
        }
        stream.println();
    }
}
