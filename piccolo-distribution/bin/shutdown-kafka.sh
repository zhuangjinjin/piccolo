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
CURRENT_DIR=`dirname "$this"`

echo $CURRENT_DIR

export BASE_DIR=`cd $(dirname $0)/..;pwd`
export KAFKA_DIR=${BASE_DIR}/kafka

function check_kafka_installation {
    if [ -d $KAFKA_DIR ]; then
        echo "true"
    else
        echo "false"
    fi
}

function stop_kafka() {
    kafka_installed=$(check_kafka_installation)
    if [[ "$kafka_installed" == "true" ]]; then
        cd $KAFKA_DIR/$KAFKA_NAME/bin
        ./kafka-server-stop.sh
        echo "Kafka stopped..."
        ./zookeeper-server-stop.sh
        echo "Zookeeper stopped..."
        cd $BASE_DIR
    else
        echo "Cannot find kafka installation. Exiting."
    fi
}

stop_kafka