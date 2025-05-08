package com.weba11y.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.weba11y.server.jpa.repository")
@EnableR2dbcRepositories(basePackages = "com.weba11y.server.r2dbc.repository")
@EnableR2dbcAuditing
public class WEBA11YServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(WEBA11YServerApplication.class, args);
	}

}
