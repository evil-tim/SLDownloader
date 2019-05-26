#!/bin/bash

cd /home/ubuntu/sldownloader-app
aws configure set region `curl -s http://169.254.169.254/latest/meta-data/placement/availability-zone | sed 's/[a-z]$//'`
$(aws ecr get-login --no-include-email)
/usr/local/bin/docker-compose up -d
