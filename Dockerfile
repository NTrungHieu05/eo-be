FROM gradle:8.0-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle bootJar --no-daemon

FROM eclipse-temurin:11-jre
WORKDIR /app
COPY --from=build /app/build/libs/eo-be.jar app.jar
ENV SPRING_PROFILES_ACTIVE=prod
ENV APP_UPLOAD_ROOT=/data
EXPOSE 8080
VOLUME ["/data"]
ENTRYPOINT ["java", "-jar", "app.jar"]
