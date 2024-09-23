# 1단계: 빌드 단계
FROM maven:3.9.3-openjdk-21 as build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# 2단계: 실행 단계
FROM openjdk:21-jdk
ARG JAR_FILE=target/*.jar
COPY --from=build /app/target/${JAR_FILE} app.jar
ENV SPRING_PROFILES_ACTIVE=real_server
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}", "/app.jar"]