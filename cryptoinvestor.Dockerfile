# syntax=docker/dockerfile:1

FROM eclipse-temurin:17-jdk-jammy

WORKDIR /cryptoinvestor
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw
RUN  ./mvnw dependency:resolve
COPY src ./src
EXPOSE 7000
ENTRYPOINT ["java" , "CryptoInvestor"]
CMD ["docker", "run" , "--rm", "cryptoinvestor" ]
