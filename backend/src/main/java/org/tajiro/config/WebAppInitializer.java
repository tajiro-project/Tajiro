package org.tajiro.config;

import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import javax.servlet.Filter;
import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletRegistration;

/**
 * web.xml 대체 진입점. 루트 컨텍스트(RootConfig, SecurityConfig)와
 * 서블릿 컨텍스트(ServletConfig)를 등록한다.
 */
public class WebAppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {
    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;
    private static final long MAX_REQUEST_SIZE = 50L * 1024 * 1024;
    private static final int FILE_SIZE_THRESHOLD = 1024 * 1024;

    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class<?>[]{RootConfig.class, org.tajiro.security.config.SecurityConfig.class};
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class<?>[]{ServletConfig.class};
    }

    @Override
    protected String[] getServletMappings() {
        return new String[]{"/"};
    }

    @Override
    protected Filter[] getServletFilters() {
        CharacterEncodingFilter filter = new CharacterEncodingFilter();
        filter.setEncoding("UTF-8");
        filter.setForceEncoding(true);
        return new Filter[]{filter};
    }

    @Override
    protected void customizeRegistration(ServletRegistration.Dynamic registration) {
        registration.setInitParameter("throwExceptionIfNoHandlerFound", "true");
        // 매물 등록(25-4) 사진 업로드용 임시 디렉터리
        String uploadDirectory = System.getProperty("java.io.tmpdir") + "/tajiro-uploads";
        registration.setMultipartConfig(new MultipartConfigElement(
                uploadDirectory,
                MAX_FILE_SIZE,
                MAX_REQUEST_SIZE,
                FILE_SIZE_THRESHOLD
        ));
    }
}
