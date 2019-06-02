#!/bin/bash

cd /home/ubuntu/sldownloader-app
aws configure set region `curl -s http://169.254.169.254/latest/meta-data/placement/availability-zone | sed 's/[a-z]$//'`
$(aws ecr get-login --no-include-email)
docker pull 535425158818.dkr.ecr.ap-southeast-1.amazonaws.com/crabranch/sldownloader:latest
docker rmi $(docker images -f "dangling=true" -q)
/usr/local/bin/docker-compose up -d
