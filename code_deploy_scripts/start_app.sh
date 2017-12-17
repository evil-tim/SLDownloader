#!/bin/bash

cd /home/ubuntu/sldownloader-app
/usr/bin/docker image build --build-arg JAR_FILE=sldownloader.jar .
/usr/local/bin/docker-compose up -d
