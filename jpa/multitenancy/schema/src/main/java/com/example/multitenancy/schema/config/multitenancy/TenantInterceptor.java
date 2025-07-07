package com.example.multitenancy.schema.config.multitenancy;

import static org.springframework.http.HttpStatus.FORBIDDEN;

import com.example.multitenancy.schema.utils.TenantNameType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
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
    public boolean preHandle(
            HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        var tenant = request.getParameter("tenant");
        if (request.getRequestURI().startsWith("/api/")) {
            if (!StringUtils.hasText(tenant)) {
                response.sendError(400, "Required parameter 'tenant' is not present.");
                return false;
            }
            if (!getValidTenants().contains(tenant)) {
                response.sendError(FORBIDDEN.value(), "Unknown schema tenant");
                return false;
            }
        }

        tenantIdentifierResolver.setCurrentTenant(tenant);

        return true;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            @Nullable Exception ex) {
        tenantIdentifierResolver.clearCurrentTenant();
    }

    private List<String> getValidTenants() {
        return List.of(TenantNameType.TEST1.getTenantName(), TenantNameType.TEST2.getTenantName());
    }
}
