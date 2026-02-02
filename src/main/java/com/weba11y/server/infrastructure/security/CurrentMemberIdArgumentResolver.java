package com.weba11y.server.infrastructure.security;

import com.weba11y.server.infrastructure.security.CurrentMemberId;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class CurrentMemberIdArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        // 파라미터에 @CurrentMemberId 어노테이션이 붙어 있는지 확인
        boolean hasAnnotation = parameter.hasParameterAnnotation(CurrentMemberId.class);

        // 파라미터 타입이 Long.class와 일치하는지 확인
        boolean isLongType = parameter.getParameterType().equals(Long.class);

        // 두 조건을 모두 만족할 때만 true 반환
        return hasAnnotation && isLongType;
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof Long) {
            return (Long) authentication.getPrincipal();
        }

        return null;
    }
}