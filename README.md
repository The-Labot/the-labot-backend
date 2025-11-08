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

---

## 도커 개발 환경 실행    

- 컨테이너 실행
```
docker-compose up -d --build
```

- 컨테이너 종료
```
docker-compose down
```

- 로컬 환경에서 컨테이너 MySQL 접속 시 3307 포트 사용

---
