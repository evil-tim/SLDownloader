FROM openjdk:8-jdk-alpine

ENTRYPOINT ["/usr/bin/java", "-jar", "/var/lib/apps/sldownloader/sldownloader.jar"]

ARG JAR_FILE
ADD target/${JAR_FILE} /var/lib/apps/sldownloader/sldownloader.jar