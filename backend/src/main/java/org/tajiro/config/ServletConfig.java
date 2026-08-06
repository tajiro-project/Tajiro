package org.tajiro.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * 서블릿 컨텍스트 - 컨트롤러 계층 스캔 + MVC/Swagger 설정 임포트.
 *
 * <p>컨트롤러 패키지를 추가하면 아래 {@code @ComponentScan} 목록에 등록하세요.
 */
@Configuration
@EnableWebMvc
@ComponentScan(basePackages = {
        "org.tajiro.auth.controller",
        "org.tajiro.terms.controller",
        "org.tajiro.user.controller",
        "org.tajiro.property.controller",
        "org.tajiro.comparison.controller",
        "org.tajiro.report.controller",
        "org.tajiro.preference.controller",
        "org.tajiro.policy.controller",
        "org.tajiro.finance.controller",
        "org.tajiro.seller.controller",
        "org.tajiro.location.controller",
        "org.tajiro.exception",
        "org.tajiro.favorite.controller"
})
@Import({WebConfig.class, SwaggerConfig.class})
public class ServletConfig {

    @Bean
    public MultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }
}
