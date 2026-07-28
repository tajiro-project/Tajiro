package org.tajiro.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.Contact;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.List;

/**
 * Springfox Swagger 2 설정.
 *
 * <ul>
 *   <li>Swagger UI : /swagger-ui.html</li>
 *   <li>API JSON   : /v2/api-docs</li>
 * </ul>
 *
 * <p>Authorization 헤더(JWT)를 UI에서 넣을 수 있도록 ApiKey를 등록해 두었습니다.
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {

    private static final String AUTH_HEADER = "Authorization";

    @Bean
    public Docket tajiroApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("org.tajiro"))
                .paths(PathSelectors.ant("/api/**"))
                .build()
                .apiInfo(apiInfo())
                .securitySchemes(List.of(apiKey()))
                .securityContexts(List.of(securityContext()))
                .useDefaultResponseMessages(false);
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("타지로 API")
                .description("지방 정착 맞춤 매물·인프라·안전·정책·금융상품 API")
                .version("1.0.0")
                .contact(new Contact("코부기", "", ""))
                .build();
    }

    private ApiKey apiKey() {
        return new ApiKey(AUTH_HEADER, AUTH_HEADER, "header");
    }

    private SecurityContext securityContext() {
        AuthorizationScope scope = new AuthorizationScope("global", "accessEverything");
        SecurityReference reference =
                new SecurityReference(AUTH_HEADER, new AuthorizationScope[]{scope});
        return SecurityContext.builder()
                .securityReferences(List.of(reference))
                .forPaths(PathSelectors.ant("/api/**"))
                .build();
    }
}
