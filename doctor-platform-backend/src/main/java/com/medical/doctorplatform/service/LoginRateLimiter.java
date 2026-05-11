package com.medical.doctorplatform.service;

import com.medical.doctorplatform.common.RateLimitExceededException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Locale;

/**
 * 使用 Redis 统计登录失败次数，达到阈值后短期锁定该用户名（防暴力破解）。
 * Redis 不可用时记录日志并跳过限制，避免开发环境无法启动业务。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LoginRateLimiter {

    private static final String KEY_PREFIX = "login:fail:";

    private final StringRedisTemplate redisTemplate;

    @Value("${app.login.max-failures:5}")
    private int maxFailures;

    @Value("${app.login.lock-ttl-minutes:15}")
    private int lockTtlMinutes;

    public void checkBlockedOrThrow(String username) {
        if (!StringUtils.hasText(username)) {
            return;
        }
        try {
            String key = key(username);
            String raw = redisTemplate.opsForValue().get(key);
            if (raw == null) {
                return;
            }
            long n = Long.parseLong(raw);
            if (n >= maxFailures) {
                throw new RateLimitExceededException(
                        "登录失败次数过多，请 " + lockTtlMinutes + " 分钟后再试");
            }
        } catch (RateLimitExceededException e) {
            throw e;
        } catch (RedisConnectionFailureException | QueryTimeoutException e) {
            log.warn("Redis 不可用，跳过登录限流检查: {}", e.getMessage());
        } catch (Exception e) {
            log.warn("登录限流检查异常，已忽略: {}", e.getMessage());
        }
    }

    public void recordFailure(String username) {
        if (!StringUtils.hasText(username)) {
            return;
        }
        try {
            String key = key(username);
            Long n = redisTemplate.opsForValue().increment(key);
            if (n != null && n == 1L) {
                redisTemplate.expire(key, Duration.ofMinutes(lockTtlMinutes));
            }
        } catch (RedisConnectionFailureException | QueryTimeoutException e) {
            log.warn("Redis 不可用，未记录登录失败次数: {}", e.getMessage());
        } catch (Exception e) {
            log.warn("记录登录失败次数异常，已忽略: {}", e.getMessage());
        }
    }

    public void clearOnSuccess(String username) {
        if (!StringUtils.hasText(username)) {
            return;
        }
        try {
            redisTemplate.delete(key(username));
        } catch (RedisConnectionFailureException | QueryTimeoutException e) {
            log.warn("Redis 不可用，未清除登录计数: {}", e.getMessage());
        } catch (Exception e) {
            log.warn("清除登录计数异常，已忽略: {}", e.getMessage());
        }
    }

    private static String key(String username) {
        return KEY_PREFIX + username.trim().toLowerCase(Locale.ROOT);
    }
}
