 set SNMP_HOME=.
 set JAVA_HOME=c:\jdk1.2.2
 echo off
 if NOT EXIST %JAVA_HOME%\bin\java.exe echo " Please set the JAVA_HOME parameter in " %0
 if NOT EXIST %JAVA_HOME%\bin\java.exe goto FINISH
 echo on 

 set CLASSPATH=%JAVA_HOME%\lib\tools.jar;.;%JAVA_HOME%\lib\dt.jar;
 :FINISH
 set JAVA_CMD= %JAVA_HOME%\bin\java -Xmx200M 

