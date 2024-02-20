FROM m.daocloud.io/docker.io/openjdk:17-jdk-slim

WORKDIR /app

COPY target/shortlink-gateway.jar /app/shortlink-gateway.jar

CMD ["java", "-jar", "shortlink-gateway.jar"]
