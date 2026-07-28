package org.tajiro.security.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

/**
 * Spring Security 설정 (JWT · 세션 미사용).
 *
 * <p>스켈레톤 단계에서는 모든 요청을 허용합니다. 인증 서비스를 구현한 뒤
 * JwtAuthenticationFilter를 UsernamePasswordAuthenticationFilter 앞에 추가하고
 * 보호가 필요한 경로를 authenticated()로 바꾸세요.
 */
@Configuration
@EnableWebSecurity
@SuppressWarnings("deprecation")
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    public void configure(WebSecurity web) {
        // Swagger 정적 리소스는 시큐리티 체인에서 제외
        web.ignoring().antMatchers(
                "/swagger-ui.html",
                "/v2/api-docs",
                "/swagger-resources/**",
                "/webjars/**"
        );
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers("/api/auth/**", "/api/terms/**").permitAll()
                .anyRequest().permitAll();

        // ------------------------------------------------------------------
        // JWT 인증을 붙일 때 아래를 함께 적용하세요.
        //
        // 1) 지금은 anyRequest().permitAll() 이라 프리플라이트(OPTIONS)도 통과하고
        //    CORS는 WebConfig.addCorsMappings(MVC 계층)가 처리합니다.
        //    보호 경로를 authenticated() 로 바꾸는 순간, 프리플라이트가 시큐리티
        //    필터에서 먼저 막혀 프론트에 CORS 에러로 보입니다. 그때 아래가 필요합니다.
        //
        //    .cors().configurationSource(corsConfigurationSource()).and()
        //    .authorizeRequests()
        //    .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
        //
        //    @Bean
        //    public CorsConfigurationSource corsConfigurationSource() {
        //        CorsConfiguration config = new CorsConfiguration();
        //        config.setAllowedOrigins(List.of("http://localhost:5173"));
        //        config.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        //        config.setAllowedHeaders(List.of("*"));
        //        config.setAllowCredentials(true);
        //        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        //        source.registerCorsConfiguration("/api/**", config);
        //        return source;
        //    }
        //
        //    ※ .cors() 를 쓸 때 CorsConfigurationSource 빈이 없으면
        //      "No bean named 'mvcHandlerMappingIntrospector' available" 로 기동에 실패합니다
        //      (SecurityConfig는 루트 컨텍스트, 해당 빈은 서블릿 컨텍스트에 있음).
        //
        // 2) http.addFilterBefore(jwtAuthenticationFilter,
        //                         UsernamePasswordAuthenticationFilter.class);
        // ------------------------------------------------------------------
    }
}
