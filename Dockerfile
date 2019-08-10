FROM openjdk:8-jdk-alpine

ENTRYPOINT [    "/usr/bin/java", \
                "-server", \
                "-Xms256m", \
                "-Xmx256m", \
                "-XX:MaxMetaspaceSize=128m", \
                "-XX:MetaspaceSize=128m", \
                "-jar", \
                "/var/lib/apps/sldownloader/sldownloader.jar", \
                "--spring.datasource.url=jdbc:h2:/var/lib/apps/sldownloader/database/database;DB_CLOSE_ON_EXIT=FALSE", \
                "--spring.thymeleaf.cache=true" \
]

ARG JAR_FILE
ADD target/${JAR_FILE} /var/lib/apps/sldownloader/sldownloader.jar
