package com.example.ultimateredis.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.example.ultimateredis.exception.RateLimitExceededException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for request admission and rejection by the rate limit interceptor. */
@ExtendWith(MockitoExtension.class)
class RateLimitInterceptorTest {

    @Mock
    private RedisRateLimiter redisRateLimiter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private RateLimitInterceptor rateLimitInterceptor;

    /**
     * Verifies that a request proceeds when the client's rate limiter grants a token.
     *
     * @throws Exception if the interceptor fails while handling the request
     */
    @Test
    void preHandle_whenAllowed_returnsTrue() throws Exception {
        given(request.getRemoteAddr()).willReturn("127.0.0.1");
        given(redisRateLimiter.tryAcquire("127.0.0.1", 10.0, 20)).willReturn(true);

        boolean result = rateLimitInterceptor.preHandle(request, response, new Object());

        assertThat(result).isTrue();
    }

    /** Verifies that an exhausted rate limit rejects the request with the expected exception. */
    @Test
    void preHandle_whenExceeded_throwsRateLimitExceededException() {
        given(request.getRemoteAddr()).willReturn("127.0.0.1");
        given(redisRateLimiter.tryAcquire("127.0.0.1", 10.0, 20)).willReturn(false);

        assertThatThrownBy(() -> rateLimitInterceptor.preHandle(request, response, new Object()))
                .isInstanceOf(RateLimitExceededException.class)
                .hasMessage("Too many requests");
    }
}
