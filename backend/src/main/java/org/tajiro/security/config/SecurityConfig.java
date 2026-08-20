package org.tajiro.security.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.tajiro.security.jwt.JwtAuthenticationFilter;
import org.tajiro.security.jwt.JwtProvider;

/**
 * Spring Security 설정 (JWT · 세션 미사용).
 *
 * <p>JwtAuthenticationFilter가 Authorization 헤더의 accessToken을 읽어 SecurityContext에
 * 인증 정보를 채운다. 다만 auth 도메인(#67) 범위에서는 다른 도메인 컨트롤러들이 아직
 * X-USER-ID 헤더 기반으로 동작 중이라 anyRequest()는 permitAll을 유지한다 — 각 도메인을
 * @AuthenticationPrincipal 기반으로 옮기는 건 별도 이슈에서 진행.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@SuppressWarnings("deprecation")
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final JwtProvider jwtProvider;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

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
                .anyRequest().permitAll()
                .and()
                .addFilterBefore(new JwtAuthenticationFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class);
    }
}
