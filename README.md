# the-labot-frontend

## 로컬 개발 환경 설정

로컬 MySQL 환경에서 Spring Boot를 실행하려면 아래 설정 파일을 직접 생성해야 합니다.
이 파일은 Git에 포함되지 않으며, 각자 로컬 환경에 맞게 설정합니다.

- 파일 생성 경로
```
src/main/resources/application-local.yaml
```

- 설정 파일 기본 형식 (application-local.yaml)
```
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/{본인_DB_이름}
    username: {본인_DB_아이디}
    password: {본인_DB_비밀번호}
    driver-class-name: com.mysql.cj.jdbc.Driver
```
