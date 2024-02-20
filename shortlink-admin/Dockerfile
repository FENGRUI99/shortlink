FROM m.daocloud.io/docker.io/openjdk:17-jdk-slim

WORKDIR /app

COPY target/shortlink-admin-1.0-SNAPSHOT.jar /app/shortlink-admin-1.0-SNAPSHOT.jar

CMD ["java", "-jar", "shortlink-admin-1.0-SNAPSHOT.jar"]
