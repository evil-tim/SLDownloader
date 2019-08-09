#!/bin/bash

# start network if needed
if [[ -z $(docker network ls | grep crabranch_network) ]]; then
    docker network create crabranch_network
fi

# configure ecr login
aws configure set region `curl -s http://169.254.169.254/latest/meta-data/placement/availability-zone | sed 's/[a-z]$//'`
$(aws ecr get-login --no-include-email)

# start
cd /home/ubuntu/sldownloader-app
/usr/local/bin/docker-compose pull
/usr/local/bin/docker-compose up -d
