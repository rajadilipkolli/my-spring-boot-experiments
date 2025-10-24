package com.example.multitenancy.partition.config.tenant;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import tools.jackson.databind.ObjectMapper;

@Configuration(proxyBeanMethods = false)
public class TenantInterceptor implements HandlerInterceptor {

    private final TenantIdentifierResolver tenantIdentifierResolver;
    private final ObjectMapper objectMapper;

    public TenantInterceptor(TenantIdentifierResolver tenantIdentifierResolver, ObjectMapper objectMapper) {
        this.tenantIdentifierResolver = tenantIdentifierResolver;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        var tenant = request.getParameter("tenant");
        String path = request.getRequestURI().substring(request.getContextPath().length());
        if (path.startsWith("/api/") && !StringUtils.hasText(tenant)) {
            ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                    HttpStatus.BAD_REQUEST, "Required parameter 'tenant' is not present.");
            problemDetail.setType(URI.create("https://multitenancy.com/errors/validation-error"));
            problemDetail.setTitle("Validation Error");
            problemDetail.setInstance(URI.create(request.getRequestURI()));

            response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(objectMapper.writeValueAsString(problemDetail));
            response.getWriter().flush();
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
