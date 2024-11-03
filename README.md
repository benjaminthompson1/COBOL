# COBOL for z/OS Repository

Enterprise COBOL codebase for z/OS with Git integration and DBB support.

## 📁 Structure
```
COBOL-for-zOS/
├── COPYLIB/        # Standardized copybooks
├── SOURCE/         # COBOL programs
├── Z-GIT/          # Z-Git directory
├── application-conf/    # DBB configuration
└── zGIT-DS-Attributes  # Dataset attributes
```

## 🚦 Quick Start

1. Clone repository
2. Configure datasets using `zGIT-DS-Attributes`
3. Set up DBB using `application-conf`
4. Reference copybooks from `COPYLIB`
5. Build source from `SOURCE` directory

## 📋 Requirements

* IBM z/OS
* Enterprise COBOL
* Git for z/OS
* DBB toolkit

## Clone the repository
```
git clone https://github.com/yourusername/COBOL-for-zOS.git
```
