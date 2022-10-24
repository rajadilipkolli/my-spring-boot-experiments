package com.example.multitenancy.partition.config;

import static org.springframework.http.HttpStatus.FORBIDDEN;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
public class TenantInterceptor implements HandlerInterceptor {

    private final TenantIdentifierResolver tenantIdentifierResolver;

    @Override
    public boolean preHandle(
            HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        var tenant = request.getParameter("tenant");
        if (!StringUtils.hasText(tenant)) {
            response.sendError(FORBIDDEN.value(), "Unknown user tenant");
            return false;
        }
        tenantIdentifierResolver.setCurrentTenant(tenant);
        return true;
    }
}
