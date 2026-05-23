# AGENTS.md

This file provides guidance to agents when working with code in this repository.

- The checked-in DBB properties describe a different tree than this workspace: [`application-conf/application.properties`](application-conf/application.properties) and [`application-conf/file.properties`](application-conf/file.properties) refer to `cics-genapp/...`, while the actual sources here live in [`SOURCE/`](SOURCE/), [`COPYLIB/`](COPYLIB/), and [`SQL/`](SQL/).
- Do not present local build, lint, or single-test commands as available. [`application-conf/application.properties`](application-conf/application.properties) enables remote zUnit with `runzTests=True`, but the referenced zUnit paths in [`application-conf/file.properties`](application-conf/file.properties) are not present in this snapshot.
- [`COPYLIB/`](COPYLIB/) is not optional supporting material; it is required context for understanding source behavior because programs use unresolved bare `COPY` statements such as [`COPY WHOAMIWS`](SOURCE/WHOAMI:18) and [`COPY DGIMMUNI`](SOURCE/DB2ARCH:67).
- [`WHOAMI`](SOURCE/WHOAMI:31) is unusual by design: it walks PSA/TCB/TIOT/CDE/ASCB/ASXB control blocks defined in [`WHOAMILK`](COPYLIB/WHOAMILK) rather than using business-level inputs.
- [`TOURFILE`](SOURCE/TOURFILE:68) looks like an ordinary batch program but is also a callable entry that accepts a linkage flag, and its validation behavior depends on the separately compiled subprogram [`CHKCODE`](SOURCE/CHKCODE:27).
- [`zGIT-DS-Attributes`](zGIT-DS-Attributes) is part of the architecture context: `SOURCE` and `COPYLIB` are defined as `PDSE FB 80 32720`, so line-oriented workstation assumptions can be misleading when explaining deployment behavior.