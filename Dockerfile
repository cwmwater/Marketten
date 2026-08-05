# Multi-stage build로 변경
# openjdk 공식 이미지는 deprecated되어 삭제됨 -> Eclipse Temurin으로 대체
FROM eclipse-temurin:17-jdk AS build

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

# 실행 스테이지 (실행에는 JDK 대신 더 가벼운 JRE만 있으면 됨)
FROM eclipse-temurin:17-jre

WORKDIR /app

# 빌드된 JAR 파일 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 설정 파일 복사
COPY src/main/resources/application*.yml ./

EXPOSE 8080

CMD ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]