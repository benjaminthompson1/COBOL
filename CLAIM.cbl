       IDENTIFICATION DIVISION.
       PROGRAM-ID.      CLAIM.
       AUTHOR.          BEN THOMPSON.
      *
      * Module 12 - Mid Term Exam
      *
       ENVIRONMENT DIVISION.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
           SELECT CLAIM-IN ASSIGN TO CLAIMIN
             ORGANIZATION IS SEQUENTIAL.
       DATA DIVISION.
       FILE SECTION.
       FD  CLAIM-IN
           RECORDING MODE IS F
           LABEL RECORDS ARE STANDARD
           RECORD CONTAINS 80 CHARACTERS
           BLOCK CONTAINS 0 RECORDS
           DATA RECORD IS CLAIM-IN-REC.
       01  CLAIM-IN-REC                      PIC X(80).
       WORKING-STORAGE SECTION.
           COPY CLAIMREC.
      * End of File switch
       77 WS-CLAIM-IN-EOF                    PIC X(01) VALUE SPACE.
          88 END-OF-FILE                     VALUE 'Y'.
       PROCEDURE DIVISION.
           PERFORM 000-HOUSEKEEPING.
           PERFORM 300-PROCESS-CLAIM UNTIL END-OF-FILE.
           GOBACK.

       000-HOUSEKEEPING.
      * Initialization Routine
           INITIALIZE CLAIM-REC.
           PERFORM 100-OPEN-FILES.
      * Priming Read
           PERFORM 200-READ-CLAIM-IN.

       100-OPEN-FILES.
           OPEN INPUT CLAIM-IN.

       200-READ-CLAIM-IN.
           READ CLAIM-IN INTO CLAIM-REC
      * Set AT END Switch
               AT END MOVE "Y" TO WS-CLAIM-IN-EOF
           END-READ.

       300-PROCESS-CLAIM.
           DISPLAY CLAIM-REC.
           IF CLAIM-AMOUNT <
           PERFORM 200-READ-CLAIM-IN.

