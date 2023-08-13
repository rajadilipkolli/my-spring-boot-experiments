/// * Licensed under Apache-2.0 2021-2023 */
// package com.example.bootr2dbc.config;
//
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.http.HttpMethod;
// import org.springframework.security.authorization.AuthorizationDecision;
// import org.springframework.security.config.web.server.ServerHttpSecurity;
// import org.springframework.security.core.Authentication;
// import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
// import org.springframework.security.core.userdetails.User;
// import org.springframework.security.core.userdetails.UserDetails;
// import org.springframework.security.crypto.factory.PasswordEncoderFactories;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.security.web.server.SecurityWebFilterChain;
// import org.springframework.security.web.server.authorization.AuthorizationContext;
// import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
// import reactor.core.publisher.Mono;
//
// @Configuration
// @Slf4j
// class SecurityConfig {
//
//    @Bean
//    SecurityWebFilterChain springWebFilterChain(ServerHttpSecurity http) {
//        var postPath = "/api/posts/**";
//        return http.csrf(ServerHttpSecurity.CsrfSpec::disable)
//                .httpBasic(httpBasicSpec ->
//                        httpBasicSpec.securityContextRepository(NoOpServerSecurityContextRepository.getInstance()))
//                .authorizeExchange(it -> it.pathMatchers(HttpMethod.GET, "/", postPath)
//                        .permitAll()
//                        .pathMatchers(HttpMethod.DELETE, postPath)
//                        .hasRole("ADMIN")
//                        .pathMatchers(postPath)
//                        .hasRole("USER")
//                        .pathMatchers("/users/{user}/**")
//                        .access(this::currentUserMatchesPath)
//                        .anyExchange()
//                        .authenticated())
//                .build();
//    }
//
//    private Mono<AuthorizationDecision> currentUserMatchesPath(
//            Mono<Authentication> authentication, AuthorizationContext context) {
//        return authentication
//                .map(a -> context.getVariables().get("user").equals(a.getName()))
//                .map(AuthorizationDecision::new);
//    }
//
//    @Bean
//    PasswordEncoder passwordEncoder() {
//        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
//    }
//
//    @Bean
//    public MapReactiveUserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
//        UserDetails user = User.withUsername("user")
//                .passwordEncoder(passwordEncoder::encode)
//                .password("password")
//                .roles("USER")
//                .build();
//        UserDetails admin = User.withUsername("admin")
//                .passwordEncoder(passwordEncoder::encode)
//                .password("password")
//                .roles("USER", "ADMIN")
//                .build();
//        log.info("user: {}", user);
//        log.info("admin: {}", admin);
//        return new MapReactiveUserDetailsService(user, admin);
//    }
// }
