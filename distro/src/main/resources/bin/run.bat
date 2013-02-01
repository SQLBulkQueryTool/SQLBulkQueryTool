@ECHO OFF
rem -------------------------------------------------------------------------
rem  This script will run the client tests 
rem
rem  To run this script:   
rem       - To use the defaults, execute:  run.bat
rem       - Optional, pass parameters, execute:  run.bat  [scenarioname] [resultmode]
rem
rem       where - scenarioname can either reference the folder or .properties file for which scenario(s) to run
rem               this would need to include the full path to the folder or file
rem             - resultmode is either;  compare, generate, sql, none
rem
rem   compare:  compare actuals to expected results
rem   generate:  create new set of expected result files in the results directory
rem   none:   execute the queries, but no comparison is done
rem   sql:    create a query file containing a query for each table exposed in DatabaseMetaData
rem
rem
rem -------------------------------------------------------------------------

rem $Id$

@if not "%ECHO%" == ""  echo %ECHO%
@if "%OS%" == "Windows_NT" setlocal

if "%OS%" == "Windows_NT" (
  set "DIRNAME=%~dp0%"
) else (
  set DIRNAME=.\
)

rem ResultMode
set RMODE=sql

rem scenario file(s) location
set S_DIR=%DIRNAME%\tests\scenarios

rem host and port to connect to
set HOST=localhost
set PORT=31000

set OUTPUTDIR=%DIRNAME%\results
set CONFIG=%DIRNAME%\..\config\test.properties

rem set USERNAME=
rem set PASSWORD=
set EXECTIMEMIN=-1
set EXCEEDPERCENT=-1

rem if passing in sceario file
if not "%1"=="" (

set S_DIR=%1

)

rem if resultmode is passed in
if not "%2"=="" (

set RMODE=%2

) 

ARGS=-Dscenario.file=%S_DIR%
ARGS=%ARGS% -Dqueryset.artifacts.dir=%QUERYSETDIR%
ARGS=%ARGS% -Dresult.mode=%RMODE%
ARGS=%ARGS% -Doutput.dir=%OUTPUTDIR%
ARGS=%ARGS% -Dconfig=%CONFIG%
ARGS=%ARGS% -Dhost.name=%HOST%
ARGS=%ARGS% -Dhost.port=%PORT%


ARGS=%ARGS% -Dexectimemin=%EXECTIMEMIN%
ARGS=%ARGS% -Dexceedpercent=%EXCEEDPERCENT%

rem If not set, the default to scenario or global properties
if not "x%USERNAME%" == "x" (
  set  ARGS=%ARGS -Dusername=%USERNAME%
)

rem If not set, the default to scenario or global properties
if not "x%PASSWORD%" == "x" (
  set  ARGS=%ARGS -Dpassword=%PASSWORD%
)

if not "x%TEIID_QUERYPLAN%" == "x" (
  set  ARGS=%ARGS -Dbqt.query.plan=true
)



set CP=%DIRNAME%/lib/*;%DIRNAME%/config/*

java -cp %CP% %JAVA_OPTS% %ARGS% org.jboss.bqt.client.TestClient
