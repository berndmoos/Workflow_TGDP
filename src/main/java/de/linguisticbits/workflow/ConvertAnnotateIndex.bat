cd /D "%~dp0"

:: The JAR compiled as target for Workflow_TGDP
set WORKFLOW_JAR="C:\Workflow_TGDP\Workflow_TGDP\target\Workflow_TGDP-1.0-SNAPSHOT.jar"

:: The directory containing all dependencies as JARs
:: The pom.xml is configured to copy all dependencies there
:: change the pom.xml as appropriate
set LIB_DIRECTORY="C:\Users\bernd\Dropbox\work\2021_MARGO_TEXAS_GERMAN\WORKFLOW_DEPENDENCIES\*"

:: The two combined
set CLASS_PATH=%WORKFLOW_JAR%;%LIB_DIRECTORY%

:: The path to the java.exe (or just 'java' if it is correctly set as a system variable)
set JAVA_CMD=java

echo ========================================== 
echo Conversion / Annotation 
echo ========================================== 

%JAVA_CMD% -classpath %CLASS_PATH% de.linguisticbits.workflow.ConvertAndAnnotate %1

echo ========================================== 
echo Fix issues (if any) that would cause trouble with the indexer
echo ========================================== 

%JAVA_CMD% -classpath %CLASS_PATH% de.linguisticbits.workflow.indexing.FixMTASIndexingProblems %1

echo ========================================== 
echo Indexing for MTAS / Annotation 
echo ========================================== 


%JAVA_CMD% -classpath %CLASS_PATH% de.linguisticbits.workflow.indexing.IndexForMTAS %2 %3 %4 %1

echo ========================================== 
echo Indexing for IDs in COMA
echo ========================================== 

%JAVA_CMD% -classpath %CLASS_PATH% de.linguisticbits.workflow.indexing.IndexForCOMA %1

echo ========================================== 
echo Stats for COMA
echo ========================================== 

%JAVA_CMD% -classpath %CLASS_PATH% de.linguisticbits.workflow.indexing.StatsForCOMA %1
