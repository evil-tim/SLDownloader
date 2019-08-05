#!/bin/bash

if [ -d "/home/ubuntu/sldownloader-app" ]; then
    cd /home/ubuntu/sldownloader-app
    /usr/local/bin/docker-compose -p crabranch down || true
fi
