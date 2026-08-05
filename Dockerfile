# Multi-stage build로 변경
FROM openjdk:17-jdk-slim as build

WORKDIR /app

# Gradle wrapper 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# 의존성 캐싱을 위해 build.gradle을 먼저 복사
RUN chmod +x gradlew

# 소스 코드 복사
COPY src src

# JAR 파일 빌드 (테스트 제외)
RUN ./gradlew build -x test --no-daemon

# 실행 스테이지
FROM openjdk:17-jdk-slim

WORKDIR /app

# 빌드된 JAR 파일 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 설정 파일 복사
COPY src/main/resources/application*.yml ./

EXPOSE 8080

CMD ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]