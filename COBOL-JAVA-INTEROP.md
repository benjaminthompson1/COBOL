# COBOL-Java Interoperability on z/OS: Technical Guide

## Overview

This guide covers COBOL-Java interoperability on z/OS using IBM JZOS Toolkit and Enterprise COBOL 6.5, including requirements, setup, and implementation patterns.

## Key IBM Documentation Sources

- **IBM JZOS Toolkit**: ibm.com/docs/en/sdk-java-technology/8
- **Enterprise COBOL for z/OS**: ibm.com/docs/en/cobol-zos/6.5
- **COBOL-Java Interoperability**: ibm.com/support/knowledgecenter
- **IBM Redbooks**: redbooks.ibm.com (SG24-7821, SG24-8337)

---

## 1. Requirements for COBOL Programs Calling Java

### Compiler Requirements
- **Enterprise COBOL 6.5** or later
- Compiler option: `THREAD` (required for JVM support)
- Compiler option: `RENT` (reentrant code required)
- Compiler option: `DLL` (for dynamic linking)
- Language Environment (LE) support for Java

### Runtime Environment
- **IBM SDK for z/OS, Java Technology Edition** (Java 8 or later recommended)
- **JZOS Toolkit** installed and configured
- Language Environment (LE) runtime with Java support
- Sufficient region size (REGION=0M recommended for JVM)

### JVM Configuration
```
JVMPROPS DD statement required with:
- java.home property
- java.class.path property
- JVM options (heap size, etc.)
```

### COBOL Program Requirements
- Must be compiled with `THREAD` option
- Must use `CALL 'JZOS...'` APIs or JNI interface
- Proper error handling for Java exceptions
- Thread-safe coding practices

### Key JZOS Classes for COBOL
- `com.ibm.jzos.ZUtil` - Utility methods
- `com.ibm.jzos.RDWInputRecordStream` - Record I/O
- `com.ibm.jzos.RDWOutputRecordStream` - Record output
- `com.ibm.jzos.ZFile` - z/OS file access

---

## 2. Requirements for Java Programs Calling COBOL

### Java Environment
- **IBM SDK for z/OS, Java Technology Edition**
- **JZOS Toolkit** classes in CLASSPATH
- JNI (Java Native Interface) support enabled

### COBOL Module Requirements
- Compiled with `DLL` option (for dynamic loading)
- Compiled with `RENT` option (reentrant)
- Exported entry points defined
- Linkage section properly defined for parameter passing

### JNI Configuration
```java
// Load native library
System.loadLibrary("cobolmodule");

// Declare native method
public native int callCobolProgram(byte[] input, byte[] output);
```

### Data Conversion Requirements
- Handle EBCDIC ↔ ASCII conversion
- Manage packed decimal (COMP-3) data
- Handle COBOL data structures (OCCURS, REDEFINES)
- Proper alignment for binary data

### JZOS Batch Launcher
- Use `com.ibm.jzos.Launcher` for batch Java programs
- Provides COBOL-like environment (DD statements, etc.)
- Handles LE initialization

---

## 3. Configuration and Setup Steps

### Step 1: Install JZOS Toolkit
```
1. Download IBM JZOS Toolkit from IBM SDK
2. Extract to USS directory (e.g., /usr/lpp/java/jzos)
3. Set JZOS_HOME environment variable
4. Add JZOS classes to CLASSPATH
```

### Step 2: Configure Java Environment
```bash
# Set Java home
export JAVA_HOME=/usr/lpp/java/J8.0_64

# Set JZOS home
export JZOS_HOME=/usr/lpp/java/jzos

# Configure CLASSPATH
export CLASSPATH=$JZOS_HOME/lib/jzos.jar:$CLASSPATH

# Set LIBPATH for native libraries
export LIBPATH=$JAVA_HOME/lib/s390x:$LIBPATH
```

### Step 3: JCL Setup for COBOL Calling Java
```jcl
//STEP1    EXEC PGM=COBOLPGM,REGION=0M
//STEPLIB  DD DSN=COBOL.LOADLIB,DISP=SHR
//         DD DSN=CEE.SCEERUN,DISP=SHR
//JAVAENV  DD *
JAVA_HOME=/usr/lpp/java/J8.0_64
CLASSPATH=/usr/lpp/java/jzos/lib/jzos.jar:/u/myapp/classes
/*
//JVMPROPS DD *
-Xms64m
-Xmx512m
-Djava.home=/usr/lpp/java/J8.0_64
/*
//SYSOUT   DD SYSOUT=*
```

### Step 4: Compile COBOL with Java Support
```jcl
//COMPILE  EXEC PGM=IGYCRCTL
//SYSIN    DD DSN=SOURCE.COBOL(MYPGM),DISP=SHR
//SYSLIN   DD DSN=&&LOADSET,DISP=(MOD,PASS)
//SYSPRINT DD SYSOUT=*
//SYSUT1-7 DD UNIT=SYSDA,SPACE=(CYL,(1,1))
//SYSPARM  DD *
  THREAD,RENT,DLL,OPTIMIZE(2)
/*
```

### Step 5: Link-Edit with LE and Java Support
```jcl
//LKED     EXEC PGM=IEWL
//SYSLIB   DD DSN=CEE.SCEELKED,DISP=SHR
//         DD DSN=CBC.SCCNOBJ,DISP=SHR
//SYSLMOD  DD DSN=LOAD.LIB(MYPGM),DISP=SHR
//SYSLIN   DD DSN=&&LOADSET,DISP=(OLD,DELETE)
//SYSPRINT DD SYSOUT=*
```

---

## 4. Enterprise COBOL 6.5 Specific Considerations

### New Features Supporting Java Interop
- **Enhanced THREAD support**: Better JVM integration
- **64-bit addressing**: Support for larger JVM heaps
- **Improved DLL support**: Better dynamic linking
- **JSON GENERATE/PARSE**: Easier data exchange with Java

### Compiler Options for COBOL 6.5
```
THREAD      - Required for JVM support
RENT        - Reentrant code
DLL         - Dynamic link library
ARCH(12)    - z/Architecture level 12
OPTIMIZE(2) - Performance optimization
SSRANGE     - Subscript range checking (recommended)
```

### JSON Support (New in COBOL 6.5)
```cobol
01 CUSTOMER-DATA.
   05 CUSTOMER-ID      PIC 9(8).
   05 CUSTOMER-NAME    PIC X(50).
   05 CUSTOMER-BALANCE PIC S9(9)V99 COMP-3.

01 JSON-OUTPUT         PIC X(1000).

* Generate JSON for Java consumption
JSON GENERATE JSON-OUTPUT FROM CUSTOMER-DATA
```

### Enhanced Error Handling
```cobol
* COBOL 6.5 provides better exception handling
EVALUATE TRUE
   WHEN JSON-STATUS = 0
      CONTINUE
   WHEN JSON-STATUS = 22
      DISPLAY 'JSON GENERATION ERROR'
   WHEN OTHER
      DISPLAY 'UNEXPECTED ERROR: ' JSON-STATUS
END-EVALUATE
```

---

## 5. JZOS Toolkit Setup and Usage

### JZOS Toolkit Components
1. **jzos.jar** - Core JZOS classes
2. **Native libraries** - JNI implementations
3. **Sample programs** - Reference implementations
4. **Launcher utility** - Batch job support

### Using JZOS in COBOL Programs

#### Method 1: Direct JNI Calls
```cobol
IDENTIFICATION DIVISION.
PROGRAM-ID. JAVACALL.

ENVIRONMENT DIVISION.
CONFIGURATION SECTION.
REPOSITORY.
    CLASS JSTRING AS "java.lang.String"
    CLASS JSYSTEM AS "java.lang.System".

DATA DIVISION.
WORKING-STORAGE SECTION.
01 JAVA-STRING        OBJECT REFERENCE JSTRING.
01 JAVA-RESULT        PIC X(100).

PROCEDURE DIVISION.
    INVOKE JSTRING "NEW" USING "Hello from COBOL"
           RETURNING JAVA-STRING
    
    INVOKE JAVA-STRING "toUpperCase"
           RETURNING JAVA-STRING
    
    INVOKE JAVA-STRING "toString"
           RETURNING JAVA-RESULT
    
    DISPLAY "Result: " JAVA-RESULT
    
    STOP RUN.
```

#### Method 2: Using JZOS Utility Classes
```cobol
* Call Java method via JZOS
CALL 'JZOS' USING
    BY CONTENT 'com.ibm.jzos.ZUtil'
    BY CONTENT 'getCurrentUser'
    BY REFERENCE WS-USER-ID
    BY REFERENCE WS-RETURN-CODE
END-CALL
```

### JZOS Batch Launcher Usage

#### Java Main Class
```java
package com.example;

import com.ibm.jzos.*;

public class BatchJob {
    public static void main(String[] args) throws Exception {
        // Access DD statements like COBOL
        ZFile input = new ZFile("//DD:INPUT", "rb");
        ZFile output = new ZFile("//DD:OUTPUT", "wb");
        
        // Process records
        byte[] record = new byte[80];
        while (input.read(record) > 0) {
            // Process record
            output.write(record);
        }
        
        input.close();
        output.close();
    }
}
```

#### JCL to Run Java with JZOS Launcher
```jcl
//JAVAJOB  JOB CLASS=A,MSGCLASS=H
//STEP1    EXEC PGM=JVMLDM80,REGION=0M
//STEPLIB  DD DSN=JZOS.LOADLIB,DISP=SHR
//         DD DSN=CEE.SCEERUN,DISP=SHR
//INPUT    DD DSN=INPUT.DATA,DISP=SHR
//OUTPUT   DD DSN=OUTPUT.DATA,DISP=(NEW,CATLG,DELETE),
//            SPACE=(TRK,(10,10)),RECFM=FB,LRECL=80
//SYSOUT   DD SYSOUT=*
//STDOUT   DD SYSOUT=*
//STDERR   DD SYSOUT=*
//JAVAENV  DD *
JAVA_HOME=/usr/lpp/java/J8.0_64
CLASSPATH=/u/myapp/classes:/usr/lpp/java/jzos/lib/jzos.jar
/*
//JAVAPARM DD *
com.example.BatchJob
/*
```

---

## 6. Code Examples and Patterns

### Pattern 1: COBOL Calling Java Method

#### COBOL Program
```cobol
IDENTIFICATION DIVISION.
PROGRAM-ID. CALLJAVAX.

ENVIRONMENT DIVISION.
CONFIGURATION SECTION.
REPOSITORY.
    CLASS MYCLASS AS "com.example.MyClass".

DATA DIVISION.
WORKING-STORAGE SECTION.
01 JAVA-OBJECT        OBJECT REFERENCE MYCLASS.
01 INPUT-DATA         PIC X(100) VALUE "Test Data".
01 OUTPUT-DATA        PIC X(100).
01 RETURN-CODE        PIC S9(9) COMP.

PROCEDURE DIVISION.
MAIN-LOGIC.
    * Create Java object
    INVOKE MYCLASS "NEW" RETURNING JAVA-OBJECT
    
    * Call Java method
    INVOKE JAVA-OBJECT "processData"
        USING BY CONTENT INPUT-DATA
        RETURNING OUTPUT-DATA
    
    * Check for exceptions
    IF JAVA-OBJECT = NULL
        DISPLAY "ERROR: Java exception occurred"
        MOVE 8 TO RETURN-CODE
    ELSE
        DISPLAY "Output: " OUTPUT-DATA
        MOVE 0 TO RETURN-CODE
    END-IF
    
    STOP RUN.
```

#### Java Class
```java
package com.example;

public class MyClass {
    public String processData(String input) {
        // Process the input
        return input.toUpperCase();
    }
}
```

### Pattern 2: Java Calling COBOL via JNI

#### Java Class with Native Method
```java
package com.example;

public class CobolCaller {
    static {
        System.loadLibrary("COBOLMOD");
    }
    
    public native int callCobol(byte[] input, byte[] output);
    
    public static void main(String[] args) {
        CobolCaller caller = new CobolCaller();
        byte[] input = "INPUT DATA".getBytes("IBM-1047");
        byte[] output = new byte[100];
        
        int rc = caller.callCobol(input, output);
        System.out.println("Return code: " + rc);
    }
}
```

#### COBOL Module (Called from Java)
```cobol
IDENTIFICATION DIVISION.
PROGRAM-ID. COBOLMOD.

DATA DIVISION.
LINKAGE SECTION.
01 INPUT-PARM         PIC X(100).
01 OUTPUT-PARM        PIC X(100).

PROCEDURE DIVISION USING INPUT-PARM OUTPUT-PARM.
    MOVE FUNCTION UPPER-CASE(INPUT-PARM) TO OUTPUT-PARM
    GOBACK.
```

### Pattern 3: Data Conversion Utilities

#### COBOL to Java Data Conversion
```cobol
* Convert COMP-3 to displayable format for Java
01 PACKED-AMOUNT      PIC S9(7)V99 COMP-3 VALUE 1234.56.
01 DISPLAY-AMOUNT     PIC -(7)9.99.

MOVE PACKED-AMOUNT TO DISPLAY-AMOUNT
* Pass DISPLAY-AMOUNT to Java
```

#### Java to COBOL Data Conversion
```java
import com.ibm.jzos.fields.*;

// Convert Java BigDecimal to COBOL COMP-3
BigDecimal amount = new BigDecimal("1234.56");
byte[] comp3 = PackedDecimal.format(amount, 9, 2);

// Convert Java String to EBCDIC
String text = "Hello";
byte[] ebcdic = text.getBytes("IBM-1047");
```

---

## 7. Common Configuration Issues and Solutions

### Issue 1: JVM Not Found
**Symptom**: `CEE3501S The module JVMLDM80 was not found`

**Solution**:
```jcl
//STEPLIB  DD DSN=JZOS.LOADLIB,DISP=SHR
//         DD DSN=CEE.SCEERUN,DISP=SHR
//         DD DSN=CBC.SCCNOBJ,DISP=SHR
```

### Issue 2: ClassNotFoundException
**Symptom**: Java class not found at runtime

**Solution**:
- Verify CLASSPATH in JAVAENV DD statement
- Ensure JAR files are in USS file system
- Check file permissions (755 for directories, 644 for files)

### Issue 3: OutOfMemoryError
**Symptom**: JVM runs out of heap space

**Solution**:
```
//JVMPROPS DD *
-Xms256m
-Xmx1024m
-Xmn128m
/*
```

### Issue 4: COBOL Program Not Reentrant
**Symptom**: `CEE3204S The system detected a protection exception`

**Solution**:
- Recompile with `RENT` option
- Ensure no WORKING-STORAGE modifications in recursive calls
- Use LOCAL-STORAGE for thread-local data

---

## 8. Performance Considerations

### JVM Startup Overhead
- JVM initialization is expensive (100-500ms)
- Reuse JVM instances when possible
- Consider persistent JVM for high-volume processing

### Data Conversion Costs
- EBCDIC ↔ ASCII conversion adds overhead
- Minimize conversions by processing in native format
- Use bulk conversion APIs when available

### Memory Management
- Set appropriate heap sizes (-Xms, -Xmx)
- Monitor garbage collection (-verbose:gc)
- Use -Xmn for young generation sizing

### Optimization Tips
```
1. Compile COBOL with OPTIMIZE(2)
2. Use ARCH(12) for latest z/Architecture features
3. Enable JIT compilation in JVM
4. Minimize object creation in tight loops
5. Use connection pooling for database access
```

---

## 9. Security Considerations

### Java Security Manager
```java
// Set security policy
System.setProperty("java.security.policy", 
    "/u/myapp/security.policy");
```

### RACF Integration
- Java programs run under USS user ID
- COBOL programs run under job user ID
- Use RACF to control resource access

### Secure Data Passing
- Encrypt sensitive data in transit
- Use secure sockets for network communication
- Clear sensitive data from memory after use

---

## 10. Testing and Debugging

### Debug COBOL with Java
```cobol
* Enable COBOL debugging
COMPILER OPTIONS: TEST(ALL,SYM)

* Add trace statements
DISPLAY "Before Java call"
INVOKE JAVA-OBJECT "method"
DISPLAY "After Java call, RC=" RETURN-CODE
```

### Debug Java with COBOL
```java
// Enable Java debugging
System.setProperty("java.compiler", "NONE");

// Add logging
import java.util.logging.*;
Logger logger = Logger.getLogger("com.example");
logger.info("Calling COBOL module");
```

### JZOS Diagnostic Tools
```
1. Enable JZOS tracing: -Djzos.trace=true
2. Check STDERR DD for Java exceptions
3. Review SYSOUT for COBOL messages
4. Use IPCS for abend analysis
```

---

## 11. Migration Path from CICS to Batch

### CICS Java Interop
```cobol
* CICS COBOL calling Java
EXEC CICS INVOKE
    CLASS 'com.example.MyClass'
    METHOD 'processData'
    USING INPUT-DATA
    RETURNING OUTPUT-DATA
END-EXEC
```

### Batch Equivalent
```cobol
* Batch COBOL calling Java via JZOS
INVOKE MYCLASS "processData"
    USING BY CONTENT INPUT-DATA
    RETURNING OUTPUT-DATA
```

---

## 12. Version Compatibility Matrix

| Component | Minimum Version | Recommended Version |
|-----------|----------------|---------------------|
| Enterprise COBOL | 5.2 | 6.5 |
| Java SDK | 7 | 8 or 11 |
| JZOS Toolkit | 2.4.8 | Latest with Java SDK |
| z/OS | 2.3 | 2.5 or 3.1 |
| Language Environment | 2.3 | 2.5 |

---

## 13. Additional Resources

### IBM Documentation
- Enterprise COBOL Programming Guide (SC27-8711)
- JZOS Toolkit User's Guide (SA23-2246)
- Java on z/OS Performance Guide (SG24-8337)
- Language Environment Programming Guide (SA38-0682)

### IBM Redbooks
- SG24-7821: Enterprise COBOL for z/OS
- SG24-8337: Java Performance on z/OS
- SG24-6854: COBOL and Java Integration

### Sample Code Locations
- JZOS samples: $JZOS_HOME/samples
- COBOL samples: /usr/lpp/cobol/samples
- IBM GitHub: github.com/ibmruntimes

---

## Summary Checklist

### For COBOL Calling Java:
- [ ] Compile with THREAD, RENT, DLL options
- [ ] Configure JAVAENV DD statement
- [ ] Set JVMPROPS with heap sizes
- [ ] Add JZOS classes to CLASSPATH
- [ ] Handle Java exceptions in COBOL
- [ ] Test with sample program

### For Java Calling COBOL:
- [ ] Compile COBOL with DLL, RENT options
- [ ] Create JNI wrapper if needed
- [ ] Load native library in Java
- [ ] Handle EBCDIC conversion
- [ ] Test data structure alignment
- [ ] Verify return codes

### General Setup:
- [ ] Install JZOS Toolkit
- [ ] Configure Java environment variables
- [ ] Set up STEPLIB concatenation
- [ ] Test with simple example
- [ ] Review performance settings
- [ ] Document configuration

---

*This guide is based on IBM z/OS best practices and Enterprise COBOL 6.5 capabilities. Always refer to the latest IBM documentation for your specific z/OS and COBOL versions.*

---

## 14. Concrete Implementation Plan: CBLCLJVA on z32a

**Environment:** z/OS 3.2 (ADCD z32a) · Enterprise COBOL V6.5 · IBM Semeru 21 · JZOS 4.0 installed

### Approach: OO COBOL INVOKE

Enterprise COBOL's built-in object-oriented `INVOKE` statement is the native mechanism for COBOL to directly instantiate Java objects and call their methods.  No middleware or wrappers are required beyond Language Environment.

```
JCL → PGM=CBLCLJVA (compiled with THREAD)
         │
         │  INVOKE COB-HELPER "new"
         ▼
      CobolHelper.class  (/u/ibmuser/cobldemo/classes/)
         │
         │  fibonacci(int)         → PIC S9(9) COMP-5
         │  toUpperCase(byte[])    ← PIC X(30)  BY REFERENCE
         │  getJvmMajorVersion()   → PIC S9(9) COMP-5
         ▼
      Results displayed in COBOL DISPLAY statements
```

**Why this approach over alternatives:**
- No pipeline or dataset exchange — COBOL and Java share the same address space
- Data flows directly between COBOL Working Storage and Java method parameters
- Single batch step; no JZOS launcher needed
- Demonstrates all three data-flow patterns: int-in/int-out, void with in-place byte[], int-out

---

### Data Type Mapping Reference

| COBOL Declaration | Java Type | Passing Convention |
|---|---|---|
| `PIC S9(9) COMP-5` | `int` | `BY VALUE` |
| `PIC S9(18) COMP-5` | `long` | `BY VALUE` |
| `COMP-1` | `float` | `BY VALUE` |
| `COMP-2` | `double` | `BY VALUE` |
| `PIC X(n)` | `byte[]` (n bytes, EBCDIC) | `BY REFERENCE` — changes in Java are visible in COBOL |
| `OBJECT REFERENCE` | Java object reference | `BY VALUE` |

The `PIC X(n) BY REFERENCE → byte[]` pattern is the key mechanism for string exchange.  Java receives the raw EBCDIC bytes, converts them via `Charset.forName("IBM-1047")`, processes them, and writes the result back into the same array.  COBOL sees the modified data when control returns.

---

### Step 1 — Java Source: CobolHelper.java

Create at `/u/ibmuser/cobldemo/com/ibm/demo/CobolHelper.java`:

```java
/*
 * CobolHelper.java
 * Demonstrates three data-flow patterns for OO COBOL INVOKE:
 *   1. int in / int out  (fibonacci)
 *   2. byte[] in-place modification  (toUpperCase)
 *   3. int out with no input  (getJvmMajorVersion)
 */
package com.ibm.demo;

import java.nio.charset.Charset;
import java.util.Arrays;

public class CobolHelper {

    private static final Charset EBCDIC = Charset.forName("IBM-1047");

    /**
     * Returns the nth Fibonacci number.
     * COBOL: PIC S9(9) COMP-5 BY VALUE → Java int.
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
     * COBOL: PIC X(30) BY REFERENCE → Java byte[30].
     * Modifications to the array are reflected back in COBOL working storage.
     */
    public void toUpperCase(byte[] data) {
        String s = new String(data, EBCDIC);
        System.out.println("CobolHelper.toUpperCase: input=[" + s.trim() + "]");
        String upper = s.toUpperCase();
        byte[] upperBytes = upper.getBytes(EBCDIC);
        Arrays.fill(data, (byte) 0x40);          // pad with EBCDIC space
        System.arraycopy(upperBytes, 0, data, 0,
                         Math.min(upperBytes.length, data.length));
    }

    /**
     * Returns the JVM major version (e.g. 21 for Java 21).
     * Confirms which JVM is active alongside COBOL at runtime.
     */
    public int getJvmMajorVersion() {
        String version = System.getProperty("java.version");
        System.out.println("CobolHelper.getJvmMajorVersion: " + version);
        return Integer.parseInt(version.split("\\.")[0]);
    }
}
```

---

### Step 2 — Compile Java in USS

```
mkdir -p /u/ibmuser/cobldemo/classes
cd /u/ibmuser/cobldemo
javac -d classes com/ibm/demo/CobolHelper.java
```

Verify the class file was created:
```
ls -al classes/com/ibm/demo/
-rw-r--r--  1 IBMUSER SYS1  <size> <date>  CobolHelper.class
```

---

### Step 3 — COBOL Source: SOURCE/CBLCLJVA

```cobol
       PROCESS THREAD,TEST,SOURCE
       IDENTIFICATION DIVISION.
       PROGRAM-ID. CBLCLJVA.
       AUTHOR. BENJAMIN THOMPSON.
       DATE-WRITTEN. 2026-05-23.
       DATE-COMPILED.
      *
      * CBLCLJVA - COBOL calling Java via OO INVOKE
      *
      * Demonstrates Enterprise COBOL 6.5 calling a Java class
      * using the INVOKE statement and REPOSITORY paragraph.
      *
      * Java class: com.ibm.demo.CobolHelper
      * Location:   /u/ibmuser/cobldemo/classes/
      * Required compiler option: THREAD
      *
       ENVIRONMENT DIVISION.
       CONFIGURATION SECTION.
       SOURCE-COMPUTER. Z32A.
       OBJECT-COMPUTER. Z32A.
       REPOSITORY.
           CLASS COB-HELPER AS "com/ibm/demo/CobolHelper".
      *
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       01 WS-HELPER-OBJ      OBJECT REFERENCE COB-HELPER.
      *
       01 WS-FIB-INPUT       PIC S9(9) COMP-5 VALUE 10.
       01 WS-FIB-RESULT      PIC S9(9) COMP-5.
       01 WS-JVM-VERSION     PIC S9(9) COMP-5.
       01 WS-INPUT-STR       PIC X(30)
                             VALUE "hello from cobol".
      *
       01 WS-DISP-N          PIC Z9.
       01 WS-DISP-FIB        PIC ZZZ9.
       01 WS-DISP-VER        PIC Z9.
      *
       PROCEDURE DIVISION.
       MAIN-PROCEDURE.
           PERFORM 1000-INITIALIZE
           PERFORM 2000-DEMO-FIBONACCI
           PERFORM 3000-DEMO-STRING
           PERFORM 4000-DEMO-JVM-VERSION
           PERFORM 9000-TERMINATE
           .
      *
       1000-INITIALIZE.
           DISPLAY "============================================"
           DISPLAY " COBOL-Java Interoperability Demo          "
           DISPLAY " CBLCLJVA  Enterprise COBOL 6.5 + Java 21  "
           DISPLAY "============================================"
           DISPLAY " "
           INVOKE COB-HELPER "new" RETURNING WS-HELPER-OBJ
           IF WS-HELPER-OBJ = NULL
               DISPLAY "ERROR: Failed to create CobolHelper object"
               MOVE 8 TO RETURN-CODE
               GOBACK
           END-IF
           DISPLAY "Java CobolHelper object created OK"
           .
      *
       2000-DEMO-FIBONACCI.
           DISPLAY " "
           DISPLAY "--- Demo 1: Integer Arithmetic via Java ---"
           INVOKE WS-HELPER-OBJ "fibonacci"
               USING BY VALUE WS-FIB-INPUT
               RETURNING WS-FIB-RESULT
           MOVE WS-FIB-INPUT  TO WS-DISP-N
           MOVE WS-FIB-RESULT TO WS-DISP-FIB
           DISPLAY "Fibonacci(" WS-DISP-N ") computed by Java = "
                   WS-DISP-FIB
           .
      *
       3000-DEMO-STRING.
           DISPLAY " "
           DISPLAY "--- Demo 2: EBCDIC String Processing via Java ---"
           DISPLAY "Before: [" WS-INPUT-STR "]"
           INVOKE WS-HELPER-OBJ "toUpperCase"
               USING BY REFERENCE WS-INPUT-STR
           DISPLAY "After:  [" WS-INPUT-STR "]"
           .
      *
       4000-DEMO-JVM-VERSION.
           DISPLAY " "
           DISPLAY "--- Demo 3: JVM Introspection from Java ---"
           INVOKE WS-HELPER-OBJ "getJvmMajorVersion"
               RETURNING WS-JVM-VERSION
           MOVE WS-JVM-VERSION TO WS-DISP-VER
           DISPLAY "JVM major version running with COBOL: " WS-DISP-VER
           .
      *
       9000-TERMINATE.
           DISPLAY " "
           DISPLAY "============================================"
           DISPLAY " CBLCLJVA completed successfully            "
           DISPLAY "============================================"
           GOBACK
           .
```

**Key COBOL constructs:**

| Construct | Purpose |
|---|---|
| `PROCESS THREAD` | Enables JVM integration and OO INVOKE support |
| `REPOSITORY. CLASS ... AS "..."` | Declares Java class using JNI slash-separated path |
| `OBJECT REFERENCE COB-HELPER` | COBOL variable holding a Java object reference |
| `INVOKE COB-HELPER "new" RETURNING ...` | Calls static factory / constructor |
| `INVOKE obj "method" USING BY VALUE ...` | Passes primitive by value (int → COMP-5) |
| `INVOKE obj "method" USING BY REFERENCE ...` | Passes PIC X address; Java byte[] modifies in-place |
| `IF WS-HELPER-OBJ = NULL` | Guards against failed Java object creation |

---

### Step 4 — Compile and Bind JCL: IUCBLCL

This project does not use a DBB pipeline.  Compilation uses the existing `IGYQCB` proc via a dedicated JCL member stored in `JCL/IUCBLCL` in this repo (upload to `IBMUSER.CNTL(IUCBLCL)` before submitting).

```jcl
//IUCBLCL JOB (FB3),'CBLCLJVA COMP-BIND',CLASS=A,MSGCLASS=H,
//        NOTIFY=&SYSUID,REGION=0M,TIME=1440
//*       TYPRUN=HOLD
//*--------------------------------------------------------------------*
//* JOB:   IUCBLCL
//* DESC:  Compile and bind CBLCLJVA - COBOL calling Java via OO INVOKE
//* AUTH:  Ben               DATE: 2026-05-24
//*
//* The THREAD compiler option is specified via the PROCESS card in
//* CBLCLJVA source - no PARM override required here.  IGYQCB's
//* bind step applies RENT by default, which is required for all
//* programs compiled with THREAD.
//*
//* Prerequisites:
//*   IBMUSER.GIT.COBOL.SOURCE(CBLCLJVA) - source loaded from git
//*   IBMUSER.GIT.COBOL.OBJECT           - object deck PDS (pre-exists)
//*   IBMUSER.GIT.COBOL.LOAD             - load library   (pre-exists)
//*--------------------------------------------------------------------*
//COBCB1 EXEC IGYQCB,
//            LNGPRFX=IGY650,
//            LIBPRF1=CEE,
//            GOPGM=CBLCLJVA
//COBOL.SYSLIB DD DISP=SHR,DSN=IBMUSER.GIT.COBOL.COPYLIB
//COBOL.SYSIN  DD DISP=SHR,DSN=IBMUSER.GIT.COBOL.SOURCE(CBLCLJVA)
//COBOL.SYSLIN DD DISP=SHR,DSN=IBMUSER.GIT.COBOL.OBJECT(CBLCLJVA)
//BIND.SYSLIB  DD DISP=SHR,DSN=IBMUSER.GIT.COBOL.LOAD
//BIND.SYSLIN  DD DISP=SHR,DSN=IBMUSER.GIT.COBOL.OBJECT(CBLCLJVA)
//BIND.SYSLMOD DD DISP=SHR,DSN=IBMUSER.GIT.COBOL.LOAD(CBLCLJVA)
//*--------------------------------------------------------------------*
//* End of Job
//*--------------------------------------------------------------------*
```

**Why no PARM change is needed for `THREAD`:**  
`CBLCLJVA` opens with `PROCESS THREAD,TEST,SOURCE`.  The compiler reads the `PROCESS` card before the JCL `PARM=`, so `THREAD` is picked up automatically — the proc runs unmodified.

**Why `RENT` is already covered:**  
`IGYQCB`'s bind step includes `RENT` in its default parms, the same as every other program compiled with this proc.  No override required.

---

### Step 5 — Execution JCL: CBLCLJVA

Stored as `JCL/CBLCLJVA` in this repo (upload to `IBMUSER.CNTL(CBLCLJVA)` before submitting):

```jcl
//CBLCLJVA JOB (FB3),'COBOL-JAVA DEMO',CLASS=A,MSGCLASS=H,
//         NOTIFY=&SYSUID,REGION=0M,TIME=1440
//*        TYPRUN=HOLD
//*--------------------------------------------------------------------*
//* JOB:   CBLCLJVA
//* DESC:  Execute CBLCLJVA - COBOL calling Java via OO INVOKE
//* AUTH:  Ben               DATE: 2026-05-24
//*--------------------------------------------------------------------*
//STEP1    EXEC PGM=CBLCLJVA
//STEPLIB  DD DISP=SHR,DSN=IBMUSER.GIT.COBOL.LOAD
//         DD DISP=SHR,DSN=CEE.SCEERUN
//*
//* CEEOPTS passes environment variables to Language Environment.
//* LE uses JAVA_HOME to locate and initialise the JVM on the first
//* INVOKE call.  CLASSPATH must be the root above com/ibm/demo/ so
//* that the JVM resolves com.ibm.demo.CobolHelper correctly.
//*
//CEEOPTS  DD *
ENVAR("JAVA_HOME=/usr/lpp/java/J21.0_64",
      "CLASSPATH=/u/ibmuser/cobldemo/classes",
      "IBM_JAVA_OPTIONS=-Xms64m -Xmx512m")
/*
//SYSOUT   DD SYSOUT=*
//STDOUT   DD SYSOUT=*
//STDERR   DD SYSOUT=*
//CEEDUMP  DD SYSOUT=*
//*--------------------------------------------------------------------*
//* End of Job
//*--------------------------------------------------------------------*
```

**Why `CEEOPTS ENVAR` not `JAVAENV` DD:**  
`JAVAENV` is specific to the JZOS Batch Launcher (`PGM=JVMLDM21`).  For OO COBOL programs running as `PGM=CBLCLJVA`, Language Environment reads environment variables from `CEEOPTS ENVAR(...)`.

---

### Expected JES Output

```
============================================
 COBOL-Java Interoperability Demo          
 CBLCLJVA  Enterprise COBOL 6.5 + Java 21  
============================================
 
Java CobolHelper object created OK

--- Demo 1: Integer Arithmetic via Java ---
CobolHelper.fibonacci(10)
Fibonacci(10) computed by Java =   55

--- Demo 2: EBCDIC String Processing via Java ---
Before: [hello from cobol               ]
CobolHelper.toUpperCase: input=[hello from cobol]
After:  [HELLO FROM COBOL               ]

--- Demo 3: JVM Introspection from Java ---
CobolHelper.getJvmMajorVersion: 21.0.8
JVM major version running with COBOL:  21

============================================
 CBLCLJVA completed successfully            
============================================
```

Lines prefixed `CobolHelper.` come from Java's `System.out` (STDOUT DD).  
COBOL `DISPLAY` output goes to SYSOUT.

---

### Troubleshooting Guide

| Symptom | Likely Cause | Resolution |
|---|---|---|
| `CEE3501S IGZCJAVA not found` | `THREAD` option missing from compile | Confirm `PROCESS THREAD` is first line of source; recompile |
| `CEE0199W JVM initialisation failed` | `JAVA_HOME` not set or wrong path | Verify `CEEOPTS ENVAR("JAVA_HOME=/usr/lpp/java/J21.0_64")` |
| `ClassNotFoundException: com/ibm/demo/CobolHelper` | Wrong or missing CLASSPATH | Confirm `/u/ibmuser/cobldemo/classes/com/ibm/demo/CobolHelper.class` exists |
| `NULL` returned after `INVOKE "new"` | Java exception in constructor | Check STDERR DD for Java stack trace |
| `S0C4` protection exception | Missing `RENT` on link-edit | Verify IGYQCB bind step PARM includes `RENT`; rebind |
| `S0C7` data exception on DISPLAY | COMP-5 display picture mismatch | Ensure display PIC matches the COMP-5 field size |
| JVM starts but `fibonacci` returns wrong value | Classpath loaded wrong class | Check for stale `.class` files; recompile Java |

---

## 15. Build Sequence Checklist

```
USS Setup  (Section 1 — COMPLETE)
──────────────────────────────────
[x] mkdir -p /u/ibmuser/cobldemo/classes
[x] Copy CobolHelper.java to USS
[x] cd /u/ibmuser/cobldemo/classes/com/ibm/demo && javac CobolHelper.java
[x] ls CobolHelper.class  (verified 2106 bytes)

Repo / Source  (Section 2 — COMPLETE)
──────────────────────────────────────
[x] Create SOURCE/CBLCLJVA  (COBOL source with PROCESS THREAD)
[x] Create JCL/IUCBLCL  (compile and bind JCL)
[x] Create JCL/CBLCLJVA  (execution JCL)

Compile and Bind  (Section 3 — NEXT)
─────────────────────────────────────
[ ] Upload SOURCE/CBLCLJVA  → IBMUSER.GIT.COBOL.SOURCE(CBLCLJVA)
[ ] Upload JCL/IUCBLCL     → IBMUSER.CNTL(IUCBLCL)
[ ] Submit IBMUSER.CNTL(IUCBLCL)
[ ] Confirm COBOL step RC=0 (THREAD accepted from PROCESS card)
[ ] Confirm BIND  step RC=0 (RENT applied by IGYQCB default)
[ ] Verify IBMUSER.GIT.COBOL.LOAD(CBLCLJVA) exists

Execution  (Section 4)
───────────────────────
[ ] Upload JCL/CBLCLJVA → IBMUSER.CNTL(CBLCLJVA)
[ ] Submit IBMUSER.CNTL(CBLCLJVA)
[ ] SYSOUT: review COBOL DISPLAY output
[ ] STDOUT: review Java System.out lines (CobolHelper.*)
[ ] Confirm job return code = 0
```

---

## 16. Extension Ideas

Once the basic demo works, the following patterns build naturally on it:

| Extension | What it demonstrates |
|---|---|
| Pass a COMP-3 amount to Java for formatting | Packed decimal → Java BigDecimal / NumberFormat |
| Call `java.util.regex` for pattern validation | Regex capability not available natively in COBOL |
| Use `com.ibm.jzos.ZUtil.getCurrentUser()` | JZOS class accessed from COBOL via INVOKE |
| Return a Java `String` via `OBJECT REFERENCE` | Full Java object lifecycle in COBOL |
| Pass a COBOL array (`OCCURS`) as byte[] | Bulk record processing across languages |