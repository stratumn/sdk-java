@REM ----------------------------------------------------------------------------
@REM ChainScript Batch script
@REM
@REM Required ENV vars:
@REM JAVA_HOME - location of a JDK home dir
@REM
@REM Valid Arguments: 
@REM   generate <output file path>
@REM   validate <input file path>	  
@REM    
@REM ----------------------------------------------------------------------------

@REM Begin all REM lines with '@' 
@echo off
@REM set title of command window
title "Build Chainscript and Run Test"
@setlocal

set ERROR_CODE=0
 

@REM Add manual JAVA Folder.
SET JAVA_FOLDER=C:\Program Files\Java\jdk1.8.0_131
 
@REM ==== START JAVAHOME VALIDATION ====
if not "%JAVA_HOME%" == "" IF exist "%JAVA_HOME%\bin\java.exe" goto OkJHome

echo.
echo Error: JAVA_HOME not found in your environment or is an invalid directory. >&2
echo Please set the JAVA_HOME variable in your environment to match the >&2
echo location of your Java installation. >&2
echo.

if not "%JAVA_FOLDER%" == "" IF exist "%JAVA_FOLDER%\bin\java.exe" (
	echo Using %JAVA_FOLDER% as java home 
	set JAVA_HOME=%JAVA_FOLDER%
 	goto OkJHome
)
goto error

:OkJHome

@REM ==== END VALIDATION ====
  
SET JAVA_EXE="%JAVA_HOME%\bin\java.exe"

@REM    -------- BUILD  -----------
 
 
Echo Building classes
	call mvnw.cmd clean
	call mvnw.cmd package install -DskipTests 
	call mvnw.cmd test 
Echo Building complete
  

@REM ---------- RUN generate / validate   json file test. ---------------
Echo Test execution phase
:RUNTEST

Set ACTION=%~1
Set FILEPATH=%~2 

If "%ACTION%" == "" goto success
If "%FILEPATH%" == "" goto success 

setLocal EnableDelayedExpansion
set CLASSPATH=
for /R  target/sdk/lib %%a in (*.jar) do (
   set CLASSPATH=!CLASSPATH!;%%a
)
set CLASSPATH="!CLASSPATH!"
@REM echo !CLASSPATH! 
  
Echo Executing SDK Test 
%JAVA_EXE%  -cp !CLASSPATH!;.\target\sdk-1.0.jar;.\target\test-classes  com.stratumn.sdk.TestSdkPojo

Goto success
  
:error
Echo No test are run.

:success
Echo Test Run Ended.
