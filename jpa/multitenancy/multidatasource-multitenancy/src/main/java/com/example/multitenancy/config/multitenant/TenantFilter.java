package com.example.multitenancy.config.multitenant;

import com.example.multitenancy.utils.AppConstants;
import com.example.multitenancy.utils.DatabaseType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.json.JsonMapper;

/**
 * Servlet filter that validates the {@code X-tenantId} header and binds the tenant identifier
 * using {@link ScopedValue} for the entire duration of the request via
 * {@link TenantContextHolder#CURRENT_TENANT}.
 *
 * <p>Unlike a {@link org.springframework.web.servlet.HandlerInterceptor}, a filter wraps
 * {@code chain.doFilter()} so the {@code ScopedValue} scope covers the full request processing
 * chain, including controller and repository code.
 */
@Component
public class TenantFilter extends OncePerRequestFilter {

    private final JsonMapper jsonMapper;
    private final List<String> validTenants;

    public TenantFilter(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
        this.validTenants = Arrays.stream(DatabaseType.values())
                .map(DatabaseType::getSchemaName)
                .toList();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String tenant = request.getHeader(AppConstants.X_TENANT_ID);
        String path = request.getRequestURI().substring(request.getContextPath().length());

        if (tenant != null && path.startsWith("/api/") && !validTenants.contains(tenant)) {
            ProblemDetail problemDetail =
                    ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "Unknown Database tenant");
            problemDetail.setType(URI.create("https://multitenancy.com/errors/tenant-error"));
            problemDetail.setTitle("Invalid Tenant");
            problemDetail.setInstance(URI.create(request.getRequestURI()));
            response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write(jsonMapper.writeValueAsString(problemDetail));
            response.getWriter().flush();
            return;
        }

        String resolvedTenant = (tenant != null) ? tenant : "unknown";
        // Bind the tenant in a ScopedValue that covers the entire filter chain,
        // including the DispatcherServlet, controller, and repository calls.
        try {
            ScopedValue.where(TenantContextHolder.CURRENT_TENANT, resolvedTenant)
                    .call(() -> {
                        chain.doFilter(request, response);
                        return null;
                    });
        } catch (IOException | ServletException | RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
