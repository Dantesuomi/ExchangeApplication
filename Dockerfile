FROM maven:3.9-amazoncorretto-21 as builder
WORKDIR /app
COPY pom.xml /app/
COPY src /app/src

RUN mvn clean install

FROM amazoncorretto:21-alpine
WORKDIR /app
COPY --from=builder /app/target/ExchangeApplication*.jar /app/app.jar

EXPOSE 8080
CMD ["java", "-jar", "app.jar"]