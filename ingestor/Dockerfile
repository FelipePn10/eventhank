FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

COPY build/libs/ingestor-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8181

ENTRYPOINT ["java","-jar","/app/app.jar"]

