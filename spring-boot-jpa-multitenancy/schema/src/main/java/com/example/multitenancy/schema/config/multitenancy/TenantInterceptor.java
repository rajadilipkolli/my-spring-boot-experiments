package com.example.multitenancy.schema.config.multitenancy;

import static org.springframework.http.HttpStatus.FORBIDDEN;

import com.example.multitenancy.schema.utils.TenantName;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
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
        if (request.getServletPath().startsWith("/api/")
                && (!StringUtils.hasText(tenant) || !getValidTenants().contains(tenant))) {
            response.sendError(FORBIDDEN.value(), "Unknown schema tenant");
            return false;
        }

        tenantIdentifierResolver.setCurrentTenant(tenant);

        return true;
    }

    private List<String> getValidTenants() {
        return List.of(TenantName.TEST1.name, TenantName.TEST2.name);
    }
}
