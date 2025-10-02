package com.example.multitenancy.db.config.multitenant;

import com.example.multitenancy.db.utils.AppConstants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.web.servlet.HandlerInterceptor;
import tools.jackson.databind.ObjectMapper;

@Configuration(proxyBeanMethods = false)
public class MultiTenantInterceptor implements HandlerInterceptor {

    private final TenantIdentifierResolver tenantIdentifierResolver;
    private final ObjectMapper objectMapper;

    public MultiTenantInterceptor(TenantIdentifierResolver tenantIdentifierResolver, ObjectMapper objectMapper) {
        this.tenantIdentifierResolver = tenantIdentifierResolver;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        var tenant = request.getHeader(AppConstants.X_TENANT_ID);
        String path = request.getRequestURI().substring(request.getContextPath().length());
        if (null != tenant && path.startsWith("/api/") && !getValidTenants().contains(tenant)) {
            ProblemDetail problemDetail =
                    ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "Unknown Database tenant");
            problemDetail.setType(URI.create("https://multitenancy.com/errors/tenant-error"));
            problemDetail.setTitle("Invalid Tenant");
            problemDetail.setInstance(URI.create(request.getRequestURI()));

            response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write(objectMapper.writeValueAsString(problemDetail));
            response.getWriter().flush();
            return false;
        }
        tenantIdentifierResolver.setCurrentTenant(tenant);
        return true;
    }

    private List<Object> getValidTenants() {
        return List.of("primary", "secondary");
    }
}
