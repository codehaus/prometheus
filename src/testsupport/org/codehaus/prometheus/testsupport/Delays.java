package org.codehaus.prometheus.testsupport;

/**
 * 
 * @author Peter Veentjer.
 */
public class Delays {
    
    public static long TINY_MS = 50;
    public static long SMALL_MS = TINY_MS * 5;//250 ms
    public static long MEDIUM_MS = TINY_MS * 10;//500 ms
    public static long LONG_MS = TINY_MS * 50;//2500 msec
    public static long EON_MS = 100000000000L;
}
