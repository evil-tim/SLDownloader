FROM maven:3.9.6-eclipse-temurin-17-alpine as builder

WORKDIR /opt/sldownloader
ADD pom.xml .
RUN mvn dependency:go-offline
ADD ./src ./src
ADD ./checkstyle.xml ./checkstyle.xml
RUN mvn clean package -Ddependency-check.skip=true -Ddockerfile.skip=true -Dmaven.test.skip=true

###################################

FROM amazoncorretto:17-alpine

COPY --from=builder /opt/sldownloader/target/sldownloader.jar /var/lib/apps/sldownloader/sldownloader.jar
ENTRYPOINT [    "/usr/bin/java", \
                "-server", \
                "-Xmx512m", \
                "-XX:MaxMetaspaceSize=512m", \
                "-jar", \
                "/var/lib/apps/sldownloader/sldownloader.jar", \
                "--spring.datasource.url=jdbc:h2:/var/lib/apps/sldownloader/database/database;DB_CLOSE_ON_EXIT=FALSE", \
                "--logging.file.name=/var/log/sldownloader/sldownloader.log" \
]

