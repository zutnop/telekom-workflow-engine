cd ..\telekom-workflow-api
call mvn clean install -DskipTests=true
if not "%ERRORLEVEL%" == "0" exit /b

cd ..\telekom-workflow-engine
call mvn clean install -DskipTests=true
if not "%ERRORLEVEL%" == "0" exit /b

cd ..\telekom-workflow-web
call mvn clean install -DskipTests=true
if not "%ERRORLEVEL%" == "0" exit /b

cd ..\telekom-workflow-test
call mvn clean install -DskipTests=true
if not "%ERRORLEVEL%" == "0" exit /b

cd ..\telekom-workflow-example
call mvn clean install -DskipTests=true
if not "%ERRORLEVEL%" == "0" exit /b