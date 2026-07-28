package org.tajiro.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * MVC 공통 설정 - CORS(프론트 dev 서버), 정적 리소스(Swagger UI), JSON 변환.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
        registry.addResourceHandler("/assets/**")
                .addResourceLocations("/assets/");
    }

    // ------------------------------------------------------------------
    // [프론트엔드용 추가] 아래 두 메서드는 참고 프로젝트(backend/)에는 없습니다.
    //
    // 프론트가 날짜를 문자열로 다루기 때문에 필요합니다.
    //   ReportListView   : new Date(report.createdAt)
    //   MyPageView       : new Date(profile.birthDate)
    //   PropertySafetyView : new Date(safety.updatedAt)
    //   PropertyDetailView : property.availableDate.replaceAll('-', '.')
    //
    // 등록하지 않으면 Jackson이 LocalDate/LocalDateTime을
    //   {"year":2026,"monthValue":7,"dayOfMonth":28,...}
    // 객체로 직렬화해서 new Date()는 Invalid Date가 되고 replaceAll은 예외가 납니다.
    // (모듈만 등록하고 WRITE_DATES_AS_TIMESTAMPS를 끄지 않으면 [2026,7,28] 배열이 됩니다.)
    //
    // DTO에서 날짜를 전부 String으로 다룰 거라면 이 두 메서드와
    // build.gradle의 jackson-datatype-jsr310 의존성을 함께 지우면 됩니다.
    // ------------------------------------------------------------------

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(new MappingJackson2HttpMessageConverter(objectMapper()));
    }
}
