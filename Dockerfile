# 1단계: 빌드 단계
FROM openjdk:21-jdk-slim as build
WORKDIR /app

# 필요한 패키지 설치 및 Maven 설치
RUN apt-get update && \
    apt-get install -y --no-install-recommends apt-utils ca-certificates wget tar && \
    wget https://archive.apache.org/dist/maven/maven-3/3.9.8/binaries/apache-maven-3.9.8-bin.tar.gz && \
    tar -xvzf apache-maven-3.9.8-bin.tar.gz && \
    mv apache-maven-3.9.8 /opt/maven && \
    ln -s /opt/maven/bin/mvn /usr/bin/mvn && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# 메모리 제한을 설정한 Maven 빌드 (MAVEN_OPTS로 메모리 최적화)
ENV MAVEN_OPTS="-Xmx512m"

# 의존성 설치 단계만 먼저 처리하여 캐시 활용
COPY pom.xml . 
RUN mvn dependency:go-offline

# 나머지 소스 코드 복사 후 빌드
COPY src ./src
RUN mvn clean package -DskipTests

# 2단계: 실행 단계
FROM openjdk:21-jdk-slim
ARG JAR_FILE=target/*.jar
COPY --from=build /app/target/${JAR_FILE} app.jar
ENV SPRING_PROFILES_ACTIVE=real_server

# 실행 시 메모리 제한 설정 (Java 애플리케이션에 대한 메모리 최적화)
ENV JAVA_OPTS="-Xms256m -Xmx512m"
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}", "/app.jar"]
