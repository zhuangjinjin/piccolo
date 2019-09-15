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
cd `dirname $0`/../target
TARGET_DIR=`pwd`

pid=`ps aux | grep -i 'piccolo.piccolo' | grep $TARGET_DIR | grep java | grep -v grep | awk '{print $2}'`
if [ -z "$pid" ]; then
    echo "No piccolo running."
    exit -1
fi

kill -15 $pid

echo "Send shutdown request to piccolo $pid ok."