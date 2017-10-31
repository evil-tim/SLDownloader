# SLDownloader

    mvn package
    sudo mkdir /etc/docker-gen
    sudo mkdir /etc/docker-gen/templates
    sudo chown ubuntu:ubuntu /etc/docker-gen/templates
    cp nginx.tmpl /etc/docker-gen/templates/nginx.tmpl
    docker-compose up -d
