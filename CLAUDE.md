# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Repository Overview

Enterprise COBOL demonstration codebase for IBM z/OS. Programs illustrate DB2 embedded SQL, IBM MQ messaging, sequential file I/O, intrinsic string/numeric functions, date/time handling, and z/OS system introspection.

## Build System

**DBB (Dependency Based Build)** — IBM's Groovy-based build framework runs on z/OS. Build configuration lives in [application-conf/](application-conf/).

Key properties files:
- [application-conf/application.properties](application-conf/application.properties) — top-level build settings, dependency resolution rules, zUnit job card
- [application-conf/Cobol.properties](application-conf/Cobol.properties) — compiler options (Enterprise COBOL V6, base params `LIB,TEST`; `SQL` appended for DB2 programs; `CICS` appended when detected)
- [application-conf/file.properties](application-conf/file.properties) — maps source file extensions to DBB language scripts; marks zUnit test cases
- [application-conf/zUnitConfig.properties](application-conf/zUnitConfig.properties) — zUnit thresholds and playback file extension (`.plbck`)

Build order defined in application.properties: `BMS.groovy → Cobol.groovy`, tests via `ZunitConfig.groovy`.  
`runzTests=True` enables automatic zUnit execution after build.

There are no local build commands — the DBB pipeline runs on z/OS and is triggered from the mainframe environment.

## Source Layout

```
SOURCE/    — 16 COBOL programs (no file extension)
COPYLIB/   — 13 shared copybooks (linkage sections, DCLGEN declarations)
SQL/       — DB2 DDL scripts
Z-GIT/     — zGit metadata (#MAKE, #IGNORE)
```

## Architecture

### Program Categories

| Category | Programs | Notes |
|---|---|---|
| System introspection | `WHOAMI` | Reads z/OS PSA/TCB structures directly |
| Date/time | `HELLOW`, `INTRDATE` | `CURRENT-DATE`, `DAY-OF-WEEK` intrinsic functions |
| String functions | `CHARFUNC`, `STRING1`, `STRING2`, `UNSTRNG`, `INSPECT1` | `UPPER/LOWER-CASE`, `REVERSE`, `NUMVAL`, `STRING`/`UNSTRING`/`INSPECT` |
| Numeric | `NUMVAL` | `NUMVAL-C` with currency symbols |
| DB2 / SQL | `DB2ARCH`, `ORGREP` | Embedded SQL, cursor processing, DCLGEN copybooks |
| IBM MQ | `CSQ4BVJ1`, `CSQ4BVK1` | MQ GET/PUT with structured error handling |
| File I/O | `TOURFILE` | Sequential file read with called subprogram `CHKCODE` |
| Subprogram | `CHKCODE` | Linkage section parameters; validates tour codes; called by `TOURFILE` |
| Utilities | `AMTOOLS` | Dynamic `CALL` to `AMRANDOM` and `AMDELAY` |

### Copybook Conventions

- **`WHOAMILK`** / **`WHOAMIWS`** — paired linkage + working-storage copybooks for PSA/TCB-based job info
- **`DG*` copybooks** (`DGIMMUNI`, `DGERISPE`, etc.) — DCLGEN-generated DB2 table host variable declarations; included in programs with embedded SQL
- Programs use `COPY <name>` without a library qualifier; the build resolves `COPYLIB` via the `COPYBOOK` dependency rule in application.properties

### Coding Patterns

- Division structure: `IDENTIFICATION` → `ENVIRONMENT` → `DATA` → `PROCEDURE`
- Working-storage sections defined before linkage; copybooks pulled in at both levels
- Paragraph naming convention: `nnnn-VERB-NOUN` (e.g., `1000-INITIALIZE`, `2000-PROCESS`, `9000-TERMINATE`)
- Programs end with `GOBACK` (subprograms) or `STOP RUN` (main programs)
- DB2 error checking: `SQLCODE` tested after every SQL statement; cursor open/fetch/close pattern used for multi-row results

## IDE Integration

[.vscode/launch.json](.vscode/launch.json) configures **Z Open Debug** parked sessions via Zowe for remote z/OS debugging. The IBM **Z Open Editor** VS Code extension provides syntax highlighting, copybook resolution, and DCLGEN support. Property groups are defined in [zapp.yaml](zapp.yaml).

## z/OS Dataset Layout

Defined in [zGIT-DS-Attributes](zGIT-DS-Attributes): all partitioned datasets use PDSE format. `SOURCE` and `COPYLIB` use FB/80 record format; `LOAD`, `OBJECT`, and `DBRMLIB` use U/0 or FB/80 as appropriate.
