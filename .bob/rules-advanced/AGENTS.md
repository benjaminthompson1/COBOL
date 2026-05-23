# AGENTS.md

This file provides guidance to agents when working with code in this repository.

- Treat [`COPYLIB/`](COPYLIB/) as a required compile dependency, not reference material; sources in [`SOURCE/`](SOURCE/) use bare `COPY` statements such as [`COPY WHOAMIWS`](SOURCE/WHOAMI:18) and [`COPY WHOAMILK`](SOURCE/WHOAMI:21).
- Do not "fix" DBB paths by assuming the current tree matches config: [`application-conf/application.properties`](application-conf/application.properties) and [`application-conf/file.properties`](application-conf/file.properties) still target `cics-genapp/...`, not this checkout's [`SOURCE/`](SOURCE/) and [`COPYLIB/`](COPYLIB/).
- Preserve positional linkage signatures for callable modules. [`CHKCODE`](SOURCE/CHKCODE:27) is called from [`TOURFILE`](SOURCE/TOURFILE:104) with ordered parameters, so reordering linkage items will silently break callers.
- Keep paragraph naming in the established numeric-hyphen form, e.g. [`000-Housekeeping`](SOURCE/DB2ARCH:81) and [`510-Immunisation-Delete`](SOURCE/DB2ARCH:159), instead of introducing generic helper names.
- For DB2 programs, keep the existing "statement then SQLCODE handling" pattern; see [`DB2ARCH`](SOURCE/DB2ARCH:133) → [`800-SQL-Return-Codes`](SOURCE/DB2ARCH:148) and [`ORGREP`](SOURCE/ORGREP:74).
- Treat [`WHOAMILK`](COPYLIB/WHOAMILK) and [`WHOAMIWS`](COPYLIB/WHOAMIWS) as layout-sensitive z/OS control-block definitions. Field offsets matter; avoid normalizing or restructuring them.
- There is no local browser- or MCP-based verification path for the main build because build/test execution is remote DBB on z/OS; use tools here for repository analysis and file edits, not for claiming a successful local compile.