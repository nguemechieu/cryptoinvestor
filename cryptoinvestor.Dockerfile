# syntax=docker/dockerfile:1

FROM eclipse-temurin:latest

WORKDIR /cryptoinvestor
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw
RUN  ./mvnw dependency:resolve
COPY src ./src
EXPOSE 7000
ENTRYPOINT ["java" ,"jar-", "cryptoinvestor-1.0-SNAPSHOT.jar"]
CMD ["docker", "run" , "--rm", "cryptoinvestor" ]
