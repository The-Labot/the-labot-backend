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

#1단계: 의존성 다운로드
#build.gradle만 먼저 복사해서 의존성을 별도 레이어로 고정한다.
#소스(src)가 바뀌어도 이 레이어는 캐시에서 재사용되고,
#build.gradle이 바뀔 때만 다시 실행된다.
FROM gradle:8.14.3-jdk21 AS deps
WORKDIR /app
COPY build.gradle settings.gradle ./
RUN gradle dependencies --no-daemon
#--no-daemon: 컨테이너는 빌드 후 종료되므로 데몬을 띄울 이유가 없음

#2단계: 빌드
#소스가 바뀌면 여기부터만 재실행된다. 의존성은 1단계 레이어에 이미 있음
FROM deps AS builder
COPY src ./src
RUN gradle bootJar --no-daemon

#3단계: 위에 생성된 jar로 실행하기
#실행만 하므로 컴파일러가 포함된 jdk 대신 jre 사용 (이미지 크기 절감)
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]