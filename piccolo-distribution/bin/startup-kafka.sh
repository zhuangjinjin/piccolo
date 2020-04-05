#!/bin/bash

#
# Copyright 2020 ukuz90
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

KAFKA_VERSION=2.4.0
KAFKA_NAME=kafka_2.12-$KAFKA_VERSION
KAFKA_FILE=$KAFKA_NAME.tgz
KAFKA_ARCHIVE_URL=https://mirrors.tuna.tsinghua.edu.cn/apache/kafka/$KAFKA_VERSION/$KAFKA_FILE
KAFKA_ARCHIVE_BACKUP_URL=https://downloads.apache.org/kafka/$KAFKA_VERSION/$KAFKA_FILE

echo $KAFKA_ARCHIVE_URL

export BASE_DIR=`cd $(dirname $0)/..;pwd`
export KAFKA_DIR=${BASE_DIR}/kafka

echo $BASE_DIR

function check_kafka_installation {
    if [ -d $KAFKA_DIR ]; then
        echo "true"
    else
        echo "false"
    fi
}

function download_kafka {
    if type curl > /dev/null 2>&1; then
        if [[ `curl -s --head $KAFKA_ARCHIVE_URL | head -n 1 2>&1 | grep "HTTP/1.[01] [23].."` ]]; then
            curl -O $KAFKA_ARCHIVE_URL
        else
            curl -O $KAFKA_ARCHIVE_BACKUP_URL
        fi
        echo "true"
    elif type wget > /dev/null 2>&1; then
        if [[ `wget -S --spider $FAIL_URL 2>&1 | grep "HTTP/1.[01] [23].."` ]]; then
            wget $KAFKA_ARCHIVE_URL
        else
            wget $KAFKA_ARCHIVE_BACKUP_URL
        fi
        echo "true"
    else
        echo "false"
    fi
}

function install_kafka {
    if [ ! -d $KAFKA_DIR ]; then
        mkdir $KAFKA_DIR
    fi
    cd $KAFKA_DIR
    echo "Downloading kafka..."
    download_successful=$(download_kafka)
    if [[ "$download_successful" == "false" ]]; then
        echo "kafka download failed - wget or curl required."
        echo "Exiting"
        exit 0
    fi
    echo $KAFKA_FILE
    tar zxvf $KAFKA_FILE
    rm $KAFKA_FILE
    if [ -h kafka ]; then
        unlink kafka
    fi
    chmod +x $KAFKA_DIR/$KAFKA_NAME/bin/kafka-server-start.sh
}

function start_kafka {
    kafka_already_installed=$(check_kafka_installation)
    if [[ "$kafka_already_installed" == "true" ]]; then
        echo "Kafka already installed. Starting kafka..."
    else
        echo "Kafka not detected."
        install_kafka
    fi
    cd $KAFKA_DIR/$KAFKA_NAME/bin
    ./zookeeper-server-start.sh -daemon $KAFKA_DIR/$KAFKA_NAME/config/zookeeper.properties
    echo "Zookeeper started..."
    ./kafka-server-start.sh -daemon $KAFKA_DIR/$KAFKA_NAME/config/server.properties
    echo "Kafka started..."
    cd $BASIC_DIR
}

start_kafka