#FROM openjdk:17-jdk-slim
#
#WORKDIR /app
#
#COPY . .
#
#RUN chmod +x gradlew #권한 부여해주기
#
#RUN ./gradlew bootJar #이게 안됨;;;
#
#COPY build/libs/*.jar app.jar
#
#ENTRYPOINT ["java", "-jar", "app.jar"]


# Gradle 공식 이미지 사용하기 위해 멀티스테이지 빌드
#1단계 빌드먼저
FROM gradle:8.14.3-jdk17 AS builder
WORKDIR /app
COPY . .
RUN gradle bootJar --no-daemon
#빌드 속도 최적화를 위해 백그라운드에서 항상 실행되는 프로세스를 사용
#매번 JVM을 새로 띄우지 않고 데몬 프로세스에 연결해서 빌드 속도 높임
#Docker 레이어는 캐시와 별개이기 때문에 안하는게 나음 (독립으로 하자)

#2단계 위애 생성된 jar로 실행하기
FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]