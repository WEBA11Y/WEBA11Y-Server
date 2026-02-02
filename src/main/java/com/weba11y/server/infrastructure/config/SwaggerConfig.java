package com.weba11y.server.infrastructure.config;


import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.parameters.Parameter;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class SwaggerConfig {
    private final String AUTH_TOKEN_HEADER = "Authorization";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("WEBA11Y API Document")
                        .version("v0.0.1")
                        .description("WEBA11Y 프로젝트의 API 명세서입니다.")
                )
                .addSecurityItem(new SecurityRequirement().addList(AUTH_TOKEN_HEADER))
                .components(new Components()
                        .addSecuritySchemes(AUTH_TOKEN_HEADER, new SecurityScheme()
                                .name(AUTH_TOKEN_HEADER)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("Bearer")));
    }

    /**
     * OpenApiCustomizer를 사용하여 특정 파라미터 (예: @CurrentMemberId가 적용된 파라미터)를 Swagger UI에서 숨깁니다.
     * 여기서는 파라미터 이름이 "memberId"라고 가정하고 숨깁니다.
     */
    @Bean
    public OpenApiCustomizer customizeOpenApi() {
        return openApi -> openApi.getPaths().values().stream()
                .flatMap(pathItem -> pathItem.readOperations().stream()) // 모든 HTTP Method (GET, POST 등)의 Operation을 가져옴
                .forEach(operation -> {
                    if (operation.getParameters() != null) {
                        // Operation의 파라미터 리스트를 스트림으로 변환하여 필터링
                        List<Parameter> filteredParameters = operation.getParameters().stream()
                                .filter(parameter -> {
                                    // "memberId"라는 이름을 가진 파라미터는 숨깁니다.
                                    // 실제 컨트롤러 메서드 파라미터 이름이 "memberId"라고 가정합니다.
                                    return !"memberId".equals(parameter.getName());
                                })
                                .collect(Collectors.toList());
                        // 필터링된 파라미터 리스트로 Operation의 파라미터 업데이트
                        operation.setParameters(filteredParameters);
                    }
                });
    }
}