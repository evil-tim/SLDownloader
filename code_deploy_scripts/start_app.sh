#!/bin/bash

cd /home/ubuntu/sldownloader-app
/usr/bin/docker image build --build-arg JAR_FILE=sldownloader.jar -t crabranch/sldownloader:0.0.3-SNAPSHOT .
/usr/local/bin/docker-compose up -d
