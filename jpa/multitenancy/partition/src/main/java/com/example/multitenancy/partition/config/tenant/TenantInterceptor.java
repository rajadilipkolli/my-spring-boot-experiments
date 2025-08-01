package com.example.multitenancy.partition.config.tenant;

import static org.springframework.http.HttpStatus.FORBIDDEN;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Configuration(proxyBeanMethods = false)
public class TenantInterceptor implements HandlerInterceptor {

    private final TenantIdentifierResolver tenantIdentifierResolver;

    public TenantInterceptor(TenantIdentifierResolver tenantIdentifierResolver) {
        this.tenantIdentifierResolver = tenantIdentifierResolver;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        var tenant = request.getParameter("tenant");
        if (request.getServletPath().startsWith("/api/") && !StringUtils.hasText(tenant)) {
            response.sendError(FORBIDDEN.value(), "Unknown user tenant");
            return false;
        }
        tenantIdentifierResolver.setCurrentTenant(tenant);
        return true;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) {
        tenantIdentifierResolver.clearCurrentTenant();
    }
}
