FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY target/api-gateway.jar app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]
