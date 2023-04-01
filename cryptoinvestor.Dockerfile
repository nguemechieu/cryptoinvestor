# syntax=docker/dockerfile:1

FROM eclipse-temurin:latest
WORKDIR /cryptoinvestor
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN mvnw  dependency:resolve
COPY src ./src
ENTRYPOINT ["java", "-jar", "cryptoinvestor-0.0.1-SNAPSHOT.jar"]