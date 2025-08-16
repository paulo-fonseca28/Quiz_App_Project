#!/usr/bin/env sh
# Gradle start up script for UN*X

DIR="$(cd "$(dirname "$0")" && pwd)"
APP_BASE_NAME=${0##*/}
APP_HOME="$DIR"

# Add default JVM options here if desired
DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'

CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

# Determine the Java command to use
if [ -n "$JAVA_HOME" ] ; then
    JAVA_BIN="$JAVA_HOME/bin/java"
else
    JAVA_BIN="java"
fi

exec "$JAVA_BIN" $DEFAULT_JVM_OPTS -cp "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
