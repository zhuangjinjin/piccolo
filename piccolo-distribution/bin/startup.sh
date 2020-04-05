#!/bin/bash

#
# Copyright 2019 ukuz90
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
#

cgwin=false
darwin=false

case "`uname`" in
CYGWIN*) cgwin=true;;
Darwin*) darwin=true;;
esac

error_exit()
{
    echo "ERROR: $1 !!!"
    exit 1
}
[ ! -e "$JAVA_HOME/bin/java" ] && JAVA_HOME=$HOME/jdk/java
[ ! -e "$JAVA_HOME/bin/java" ] && JAVA_HOME=/usr/java
[ ! -e "$JAVA_HOME/bin/java" ] && unset JAVA_HOME

if [ -z "$JAVA_HOME" ]; then
    if $darwin; then
        if [ -x '/usr/libexec/java_home' ]; then
            export JAVA_HOME = `/usr/libexec/java_home`

        elif [ -d "/System/Library/Frameworks/JavaVM.framework/Versions/CurrentJDK/Home" ]; then
            export JAVA_HOME = "/System/Library/Frameworks/JavaVM.framework/Versions/CurrentJDK/Home"
        fi
    fi

    if [ -z "$JAVA_HOME" ]; then
        error_exit "Please set the JAVA_HOME variable in your environment, We need java(x64)! jdk8 or later is better!"
    fi
fi

export SERVER="piccolo-server-*"
export MODE="cluster"
export FLAG="non-daemon"
export JAVA_HOME
export JAVA="$JAVA_HOME/bin/java"
export METRIC_PORT=8086
export METRIC_CONTEXT_PATH=/piccolo/actuator

while getopts ":m:f:" opt
do
    case $opt in
        m) MODE=$OPTARG;;
        f) FLAG=$OPTARG;;
        ?) error_exit "Unknown parameter";;
    esac
done

export BASE_DIR=`cd $(dirname $0)/..;pwd`
export CONF="$BASE_DIR/conf/piccolo-server.properties"

#===========================================================================================
# JVM Configuration
#===========================================================================================

if [[ $MODE == "standalone" ]]; then
    JAVA_OPT="$JAVA_OPT -Xms512m -Xmx512m -Xmn256m"
else
    JAVA_OPT="$JAVA_OPT -server -Xms2g -Xmx2g -Xmn1g -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=320m"
    JAVA_OPT="$JAVA_OPT -XX:-OmitStackTraceInFastThrow -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=$BASE_DIR/logs/java_heapdump.prof"
    JAVA_OPT="$JAVA_OPT -XX:-UseLargePages"
fi

JAVA_MAJOR_VERSION=`$JAVA -version 2>&1 | sed -E -n 's/.* version "([0-9]*).*$/\1/p'`
if [ $JAVA_MAJOR_VERSION -ge "9" ]; then
    JAVA_OPT="$JAVA_OPT -cp .:${BASE_DIR}/lib/*.jar"
    JAVA_OPT="$JAVA_OPT -Xlog:gc*:file=${BASE_DIR}/logs/piccolo_gc.log:time,tags:filecount=10,filesize=102400"
else
    JAVA_OPT="$JAVA_OPT -Djava.ext.dirs=${JAVA_HOME}/jre/lib/ext:${JAVA_HOME}/lib/ext:${BASE_DIR}/lib"
    JAVA_OPT="$JAVA_OPT -Xloggc:${BASE_DIR}/logs/piccolo_gc.log -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintGCTimeStamps -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=10 -XX:GCLogFileSize=100M"
fi

if [ -f "$CONF" ]; then
    JAVA_OPT="$JAVA_OPT -Dpiccolo.server.conf=$CONF"
fi

JAVA_OPT="$JAVA_OPT -Dserver.port=$METRIC_PORT -Dmanagement.endpoints.web.basePath=$METRIC_CONTEXT_PATH -Djava.net.preferIPv4Stack=true  -jar $BASE_DIR/lib/$SERVER.jar"

if [ ! -d "$BASE_DIR/logs" ]; then
    mkdir -p $BASE_DIR/logs
fi

echo "$JAVA $JAVA_OPT"

if [[ $MODE == "standalone" ]]; then
    echo "piccolo is start with standalone."
else
    echo "piccolo is start with cluster."
fi

if [[ ! -f "$BASE_DIR/logs/start.out" ]]; then
    touch "$BASE_DIR/logs/start.out"
fi

if [[ $FLAG == "daemon" ]]; then
    nohup $JAVA $JAVA_OPT piccolo.piccolo >> ${BASE_DIR}/logs/start.out 2>&1 &
    echo "piccolo is starting，you can check the ${BASE_DIR}/logs/start.out"
else
    $JAVA $JAVA_OPT
fi