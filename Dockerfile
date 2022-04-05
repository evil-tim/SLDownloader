FROM amazoncorretto:17-alpine

ARG JAR_FILE
ADD target/${JAR_FILE} /var/lib/apps/sldownloader/sldownloader.jar

ENTRYPOINT [    "/usr/bin/java", \
                "-server", \
                "-Xmx128m", \
                "-XX:MaxMetaspaceSize=128m", \
                "-jar", \
                "/var/lib/apps/sldownloader/sldownloader.jar", \
                "--spring.datasource.url=jdbc:h2:/var/lib/apps/sldownloader/database/database;DB_CLOSE_ON_EXIT=FALSE", \
                "--spring.thymeleaf.cache=true", \
                "--logging.file.name=/var/log/sldownloader/sldownloader.log" \
]
