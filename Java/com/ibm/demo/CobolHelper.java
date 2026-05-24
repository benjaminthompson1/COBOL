/*
 * CobolHelper.java
 *
 * Java utility class called from Enterprise COBOL 6.5 via OO INVOKE.
 *
 * Demonstrates three data-flow patterns for COBOL-Java interoperability:
 *   1. int in / int out     fibonacci(int)
 *   2. byte[] in-place      toUpperCase(byte[])
 *   3. int out, no input    getJvmMajorVersion()
 *
 * USS location: /u/ibmuser/cobldemo/com/ibm/demo/CobolHelper.java
 * Compile:      javac -d /u/ibmuser/cobldemo/classes \
 *                        /u/ibmuser/cobldemo/com/ibm/demo/CobolHelper.java
 *
 * Called by:    SOURCE/CBLCLJVA  (COBOL, compiled with THREAD option)
 */
package com.ibm.demo;

import java.nio.charset.Charset;
import java.util.Arrays;

public class CobolHelper {

    private static final Charset EBCDIC = Charset.forName("IBM-1047");

    /**
     * Returns the nth Fibonacci number.
     *
     * COBOL mapping:
     *   INVOKE WS-HELPER-OBJ "fibonacci"
     *       USING BY VALUE WS-FIB-INPUT       (PIC S9(9) COMP-5)
     *       RETURNING WS-FIB-RESULT            (PIC S9(9) COMP-5)
     */
    public int fibonacci(int n) {
        System.out.println("CobolHelper.fibonacci(" + n + ")");
        if (n <= 1) return n;
        int a = 0, b = 1;
        for (int i = 2; i <= n; i++) {
            int c = a + b;
            a = b;
            b = c;
        }
        return b;
    }

    /**
     * Upper-cases EBCDIC bytes in-place.
     *
     * COBOL passes PIC X(30) BY REFERENCE; Java receives the raw EBCDIC
     * byte array.  Modifications are written back into COBOL working
     * storage because BY REFERENCE passes the storage address directly.
     *
     * COBOL mapping:
     *   INVOKE WS-HELPER-OBJ "toUpperCase"
     *       USING BY REFERENCE WS-INPUT-STR   (PIC X(30))
     */
    public byte[] toUpperCase(byte[] data) {
        String s = new String(data, EBCDIC);
        System.out.println("CobolHelper.toUpperCase: input=[" + s.trim() + "]");
        byte[] result = new byte[data.length];
        Arrays.fill(result, (byte) 0x40);
        byte[] upperBytes = s.toUpperCase().getBytes(EBCDIC);
        System.arraycopy(upperBytes, 0, result, 0,
                         Math.min(upperBytes.length, result.length));
        return result;
    }

    /**
     * Returns the JVM major version number (e.g. 21 for Java 21).
     *
     * Useful to confirm which JVM is initialised alongside the COBOL job.
     *
     * COBOL mapping:
     *   INVOKE WS-HELPER-OBJ "getJvmMajorVersion"
     *       RETURNING WS-JVM-VERSION           (PIC S9(9) COMP-5)
     */
    public int getJvmMajorVersion() {
        String version = System.getProperty("java.version");
        System.out.println("CobolHelper.getJvmMajorVersion: " + version);
        return Integer.parseInt(version.split("\\.")[0]);
    }
}
