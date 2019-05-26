#!/bin/bash

cd /home/ubuntu/sldownloader-app
$(aws ecr get-login)
/usr/local/bin/docker-compose up -d
