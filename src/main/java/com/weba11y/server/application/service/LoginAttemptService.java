package com.weba11y.server.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private static final String LOGIN_ATTEMPT_PREFIX = "login_attempt:";
    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCK_DURATION_MINUTES = 30;

    private final StringRedisTemplate redisTemplate;

    public boolean isBlocked(String userId) {
        String key = LOGIN_ATTEMPT_PREFIX + userId;
        String attempts = redisTemplate.opsForValue().get(key);
        return attempts != null && Integer.parseInt(attempts) >= MAX_ATTEMPTS;
    }

    public void loginFailed(String userId) {
        String key = LOGIN_ATTEMPT_PREFIX + userId;
        Long attempts = redisTemplate.opsForValue().increment(key);
        if (attempts != null && attempts == 1) {
            redisTemplate.expire(key, LOCK_DURATION_MINUTES, TimeUnit.MINUTES);
        }
    }

    public void loginSucceeded(String userId) {
        String key = LOGIN_ATTEMPT_PREFIX + userId;
        redisTemplate.delete(key);
    }
}
