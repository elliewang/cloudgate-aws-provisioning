FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn -B -ntp -DskipTests package
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd -r -u 10001 portal
COPY --from=builder /build/target/cloud-portal-1.0.0.jar /app/portal.jar
USER portal
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/portal.jar"]
