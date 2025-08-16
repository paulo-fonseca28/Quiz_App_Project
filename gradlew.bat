\
@ECHO OFF
SETLOCAL

SET DIR=%~dp0
SET APP_HOME=%DIR%

SET DEFAULT_JVM_OPTS="-Xmx64m" "-Xms64m"
SET CLASSPATH=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar

IF NOT "%JAVA_HOME%"=="" (
  SET JAVA_EXE=%JAVA_HOME%\bin\java.exe
) ELSE (
  SET JAVA_EXE=java.exe
)

"%JAVA_EXE%" %DEFAULT_JVM_OPTS% -cp "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*
