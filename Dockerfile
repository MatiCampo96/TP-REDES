FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
COPY index.html script.js styles.css imagen.png ./
COPY config ./config
RUN mvn -B package && cp target/tp-redes-*.jar app.jar

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/app.jar ./app.jar
COPY index.html script.js styles.css imagen.png ./
COPY config ./config
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
