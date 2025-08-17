FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY target/order-service.jar app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]
