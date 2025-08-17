FROM maven:3.9.5-eclipse-temurin-21-alpine as build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY --from=build /app/target/driver-service.jar app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]
