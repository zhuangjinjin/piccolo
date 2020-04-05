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

NACOS_VERSION=1.2.1
NACOS_NAME=nacos-server-$NACOS_VERSION
NACOS_FILE=$NACOS_NAME.zip
NACOS_ARCHIVE_URL=https://github.com/alibaba/nacos/releases/download/$NACOS_VERSION/$NACOS_FILE

export BASE_DIR=`cd $(dirname $0)/..;pwd`
export NACOS_DIR=${BASE_DIR}/nacos

function check_nacos_installation {
    if [ -d $NACOS_DIR ]; then
        echo "true"
    else
        echo "false"
    fi
}

function download_nacos {
    if type curl > /dev/null 2>&1; then
        if [[ `curl -s --head $NACOS_ARCHIVE_URL | head -n 1 2>&1 | grep "HTTP/1.[01] [23].."` ]]; then
            curl -O $NACOS_ARCHIVE_URL
        fi
        echo "true"
    elif type wget > /dev/null 2>&1; then
        if [[ `wget -S --spider $FAIL_URL 2>&1 | grep "HTTP/1.[01] [23].."` ]]; then
            wget $NACOS_ARCHIVE_URL
        fi
        echo "true"
    else
        echo "false"
    fi
}

function install_nacos {
    if [ ! -d $NACOS_DIR ]; then
        mkdir $NACOS_DIR
    fi
    cd $NACOS_DIR
    echo "Downloading nacos..."
    download_successful=$(download_nacos)
    if [[ "$download_successful" == "false" ]]; then
        echo "Nacos download failed - wget or curl required."
        echo "Exiting"
        exit 0
    fi
    unzip $NACOS_FILE
    rm $NACOS_FILE
    chmod +x $NACOS_DIR/nacos/bin/startup.sh
}

function start_nacos {
    nacos_already_installed=$(check_nacos_installation)
    if [[ "$nacos_already_installed" == "true" ]]; then
        echo "Nacos already installed. Starting nacos..."
    else
        echo "Nacos not detected."
        install_nacos
    fi
    cd $NACOS_DIR/nacos/bin
    ./startup.sh -daemon
    echo "Nacos started..."
    cd $BASIC_DIR
}

start_nacos