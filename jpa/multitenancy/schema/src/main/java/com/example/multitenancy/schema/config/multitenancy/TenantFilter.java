package com.example.multitenancy.schema.config.multitenancy;

import com.example.multitenancy.schema.utils.TenantNameType;
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
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.json.JsonMapper;

@Component
public class TenantFilter extends OncePerRequestFilter {

    private final JsonMapper jsonMapper;

    public static final ScopedValue<String> CURRENT_TENANT = ScopedValue.newInstance();

    public TenantFilter(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException {
        var tenant = request.getParameter("tenant");
        String path = request.getRequestURI().substring(request.getContextPath().length());
        if (path.startsWith("/api/")) {
            if (!StringUtils.hasText(tenant)) {
                writeProblem(
                        response,
                        request.getRequestURI(),
                        HttpStatus.BAD_REQUEST,
                        "Validation Error",
                        "Required parameter 'tenant' is not present.",
                        "https://multitenancy.com/errors/validation-error");
                return;
            }
            if (!getValidTenants().contains(tenant)) {
                writeProblem(
                        response,
                        request.getRequestURI(),
                        HttpStatus.FORBIDDEN,
                        "Invalid Tenant",
                        "Unknown Database tenant",
                        "https://multitenancy.com/errors/tenant-error");
                return;
            }
        }

        ScopedValue.where(CURRENT_TENANT, tenant).run(() -> {
            try {
                filterChain.doFilter(request, response);
            } catch (IOException | ServletException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void writeProblem(
            HttpServletResponse response,
            String requestUri,
            HttpStatus status,
            String title,
            String detail,
            String typeUrl)
            throws IOException {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setType(URI.create(typeUrl));
        problemDetail.setTitle(title);
        problemDetail.setInstance(URI.create(requestUri));

        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setStatus(status.value());
        response.getWriter().write(jsonMapper.writeValueAsString(problemDetail));
        response.getWriter().flush();
    }

    private List<String> getValidTenants() {
        return Arrays.stream(TenantNameType.values())
                .map(TenantNameType::getTenantName)
                .toList();
    }
}
