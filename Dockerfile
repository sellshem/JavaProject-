FROM maven:3.9.8-eclipse-temurin-17 AS build

WORKDIR /app
COPY pom.xml ./
COPY src ./src
RUN mvn -B -q -DskipTests package

FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY --from=build /app/target/qazaq-learn-0.0.1-SNAPSHOT.jar ./qazaq-learn.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "qazaq-learn.jar"]
