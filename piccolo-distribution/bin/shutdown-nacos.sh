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
NACOS_NAME=nacos
CURRENT_DIR=`dirname "$this"`

export BASE_DIR=`cd $(dirname $0)/..;pwd`
export NACOS_DIR=${BASE_DIR}/nacos

function check_nacos_installation {
    if [ -d $NACOS_DIR ]; then
        echo "true"
    else
        echo "false"
    fi
}

function stop_nacos() {
    nacos_installed=$(check_nacos_installation)
    if [[ "$nacos_installed" == "true" ]]; then
        cd $NACOS_DIR/$NACOS_NAME/bin
        ./shutdown.sh
        echo "Nacos stopped..."
        cd $BASE_DIR
    else
        echo "Cannot find Nacos installation. Exiting."
    fi
}

stop_nacos