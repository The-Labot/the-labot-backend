package com.example.the_labot_backend;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class TheLabotBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(TheLabotBackendApplication.class, args);
	}
	@PostConstruct
	public void init() {
		// 애플리케이션 전역에서 TimeZone을 한국 시간으로 설정
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
		System.out.println("현재 시간대: " + TimeZone.getDefault().getID());
	}

}
