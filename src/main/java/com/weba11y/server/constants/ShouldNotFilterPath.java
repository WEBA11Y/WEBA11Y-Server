package com.weba11y.server.constants;

import java.util.Arrays;
import java.util.List;

public class ShouldNotFilterPath {
    public static final List<String> EXCLUDE_PATHS = Arrays.asList(
            "/v1/api-docs",
            "/swagger-ui/**",
            "/api/v1/join",
            "/api/v1/login",
            "/api/v1/join/**"
    );
}

