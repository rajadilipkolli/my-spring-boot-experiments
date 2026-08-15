package com.example.multitenancy.config.multitenant;

import com.example.multitenancy.utils.AppConstants;
import com.example.multitenancy.utils.DatabaseType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.web.servlet.HandlerInterceptor;
import tools.jackson.databind.json.JsonMapper;

@Configuration(proxyBeanMethods = false)
public class MultiTenantInterceptor implements HandlerInterceptor {

    private final TenantIdentifierResolver tenantIdentifierResolver;
    private final JsonMapper jsonMapper;

    public MultiTenantInterceptor(TenantIdentifierResolver tenantIdentifierResolver, JsonMapper jsonMapper) {
        this.tenantIdentifierResolver = tenantIdentifierResolver;
        this.jsonMapper = jsonMapper;
    }

    private List<String> validTenantsList = new ArrayList<>();

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
            response.getWriter().write(jsonMapper.writeValueAsString(problemDetail));
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

    private List<String> getValidTenants() {
        if (validTenantsList.isEmpty()) {
            validTenantsList = Arrays.stream(DatabaseType.values())
                    .map(DatabaseType::getSchemaName)
                    .toList();
        }
        return validTenantsList;
    }
}
