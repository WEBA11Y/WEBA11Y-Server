package com.weba11y.server;

import com.weba11y.server.configuration.EnvLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WEBA11YServerApplication {

	public static void main(String[] args) {
		// Spring Boot 실행 전에 .env 파일을 환경 변수로 로드
		EnvLoader.loadEnv("src/main/resources/.env");
		SpringApplication.run(WEBA11YServerApplication.class, args);
	}

}
