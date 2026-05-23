# AGENTS.md

This file provides guidance to agents when working with code in this repository.

- Treat the checked-in DBB config as historical context, not an accurate local architecture map: [`application-conf/application.properties`](application-conf/application.properties) and [`application-conf/file.properties`](application-conf/file.properties) still describe `cics-genapp/...` locations instead of this repo's [`SOURCE/`](SOURCE/) and [`COPYLIB/`](COPYLIB/).
- There is no meaningful local execution architecture to plan around. Build and zUnit execution are expected on z/OS DBB, and the referenced zUnit directories in [`application-conf/file.properties`](application-conf/file.properties) are missing from this checkout.
- Copybooks are an architectural dependency, not just shared types: bare `COPY` statements mean [`COPYLIB/`](COPYLIB/) must be modeled as part of every compilation and impact-analysis flow.
- Callable modules depend on positional linkage contracts across files. [`TOURFILE`](SOURCE/TOURFILE:104) calls [`CHKCODE`](SOURCE/CHKCODE:27) by ordered arguments, so interface changes require coordinated updates to both caller and callee.
- DB2 flow is intentionally stepwise rather than abstracted: [`DB2ARCH`](SOURCE/DB2ARCH:133) performs one SQL operation at a time with explicit post-statement checks, and archive behavior is controlled by DB2 global variables such as [`SYSIBMADM.MOVE_TO_ARCHIVE`](SOURCE/DB2ARCH:170) and [`SYSIBMADM.GET_ARCHIVE`](SOURCE/DB2ARCH:206).
- [`WHOAMI`](SOURCE/WHOAMI:31) is coupled to z/OS control-block layouts in [`WHOAMILK`](COPYLIB/WHOAMILK) and [`WHOAMIWS`](COPYLIB/WHOAMIWS); redesigns that treat it as ordinary business logic will miss that dependency.
- Dataset shape is part of the deployment design: [`zGIT-DS-Attributes`](zGIT-DS-Attributes) fixes `SOURCE` and `COPYLIB` at `PDSE FB 80 32720`, which matters when planning tooling or formatting changes.