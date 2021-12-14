::
:: Batch script to use mvn to build CRCL XML extensions jar
:: 

cd /d %~dp0

:: Find the name of the Netbeans folder including version
set dir="C:\Program Files\NetBeans"
for /d %%a in (%dir%*) do (set name=%%a)

echo NetBeans folder is %name%




set "PATH=%name%\netbeans\java\maven\bin;%PATH%"
echo PATH=%PATH%

call mvn clean
pause
call mvn package

pause
