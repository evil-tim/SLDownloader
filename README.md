# SLDownloader

    mvn package
    docker volume create --name sldownloader-database
    docker run -d -p 80:8080 -v sldownloader-database:/var/lib/apps/sldownloader/database crabranch/sldownloader:0.0.2-SNAPSHOT
