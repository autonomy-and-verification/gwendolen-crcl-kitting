

:: assign root environment folder to parent folder
FOR /f "usebackq tokens=*" %%x IN (`cd`) DO SET root=%%x
FOR /f "usebackq tokens=*" %%x IN (`cd`) DO SET bin=%%x\..\..\..\..\..\bin
FOR /f "usebackq tokens=*" %%x IN (`cd`) DO SET lib=%%x\..\..\..\..\..\lib

set crcl=%lib%\crcl
set eis=%lib%\eis
set thrdparty=%lib%\3rdparty
set JAVA_HOME=C:\Program Files\AdoptOpenJDK\jdk-8.0.272.10-hotspot\
set JRE=C:\Users\michalos\.p2\pool\plugins\org.eclipse.justj.openjdk.hotspot.jre.full.win32.x86_64_17.0.1.v20211116-1657\jre

set thirdptylibs=%thrdparty%\RunJPF.jar;
set thirdptylibs=%thirdptylibs%%thrdparty%\antlr-4.7-complete.jar;
set thirdptylibs=%thirdptylibs%%thrdparty%\commons-io-2.4.jar;
set thirdptylibs=%thirdptylibs%%thrdparty%\eis-0.5.0.jar;
set thirdptylibs=%thirdptylibs%%thrdparty%\ev3classes.jar;
set thirdptylibs=%thirdptylibs%%thrdparty%\ev3classes.jar;
set thirdptylibs=%thirdptylibs%%thrdparty%\ev3tools.jar;
set thirdptylibs=%thirdptylibs%%thrdparty%\jackson-databind-2.9.9.3.jar;
set thirdptylibs=%thirdptylibs%%thrdparty%\java-prolog-parser.jar;
set thirdptylibs=%thirdptylibs%%thrdparty%\java_rosbridge_all.jar;
set thirdptylibs=%thirdptylibs%%thrdparty%\jpf-annotations.jar;
set thirdptylibs=%thirdptylibs%%thrdparty%\jpf-classes.jar;
set thirdptylibs=%thirdptylibs%%thrdparty%\jpf.jar;
set thirdptylibs=%thirdptylibs%%thrdparty%\jpl.jar;
set thirdptylibs=%thirdptylibs%%thrdparty%\junit-4.10.jar;
set thirdptylibs=%thirdptylibs%%thrdparty%\system-rules-1.16.0.jar;  

::echo crlibs %thirdptylibs%
::pause
::commons-cli-1.4-sources.jar;
set crcllibs=%crcllibs%%crcl%\commons-cli-1.4.jar;
::commons-math3-3.6.1-sources.jar;
set crcllibs=%crcllibs%%crcl%\commons-math3-3.6.1.jar;
::crcl4java-base-1.9.1-SNAPSHOT-sources.jar;
set crcllibs=%crcllibs%%crcl%\crcl4java-base-1.9.1-SNAPSHOT.jar;
set crcllibs=%crcllibs%%crcl%\crcl4java-utils-1.9.1-SNAPSHOT-jar-with-dependencies.jar;
::crcl4java-utils-1.9.1-SNAPSHOT-sources.jar;
set crcllibs=%crcllibs%%crcl%\crcl4java-utils-1.9.1-SNAPSHOT.jar;
set crcllibs=%crcllibs%%crcl%\rcsjava.jar;


set cp="%bin%;%thrdparty%\RunJPF.jar;%thrdparty%\jpf.jar;%thrdparty%\antlr-4.7-complete.jar;%thrdparty%\eis-0.5.0.jar;%crcl%\*.jar" 

set javahome="C:\Program Files\AdoptOpenJDK\jdk-8.0.272.10-hotspot\bin"

C:\Users\michalos\.p2\pool\plugins\org.eclipse.justj.openjdk.hotspot.jre.full.win32.x86_64_17.0.1.v20211116-1657\jre\bin\java.exe  ^
-ea  "-javaagent:C:\Users\michalos\eclipse\java-2021-092\eclipse\configuration\org.eclipse.osgi\222\0\.cp\lib\javaagent-shaded.jar" ^
-Dfile.encoding=Cp1252 "-Djava.library.path=C:\Users\louiseadennis\Systems\swipl-devel\packages\jpl" ^
-cp "%bin%;%crcllibs%; %eis%\*.jar;%thirdptylibs%" ^
 ail.mas.AIL "%root%\robot.ail" ^
-log ^
-buildinfo

pause

