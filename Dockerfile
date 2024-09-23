# 1단계: 빌드 단계
FROM openjdk:21 as build
WORKDIR /app

# Maven 3.9.8 설치
RUN apt-get update && \
    apt-get install -y wget && \
    wget https://archive.apache.org/dist/maven/maven-3/3.9.8/binaries/apache-maven-3.9.8-bin.tar.gz && \
    tar -xvzf apache-maven-3.9.8-bin.tar.gz && \
    mv apache-maven-3.9.8 /opt/maven && \
    ln -s /opt/maven/bin/mvn /usr/bin/mvn

# Maven 빌드 수행
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
