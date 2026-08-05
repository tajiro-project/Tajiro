package org.tajiro.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * 루트 컨텍스트 - DataSource, MyBatis, 트랜잭션, 서비스 계층 스캔.
 *
 * <p>도메인 패키지를 추가하면 아래 {@code @MapperScan} / {@code @ComponentScan}
 * 목록에 등록하세요.
 */
@Configuration
@PropertySource("classpath:application.properties")
@EnableTransactionManagement
@MapperScan(basePackages = {
        "org.tajiro.auth.mapper",
        "org.tajiro.terms.mapper",
        "org.tajiro.user.mapper",
        "org.tajiro.property.mapper",
        "org.tajiro.comparison.mapper",
        "org.tajiro.report.mapper",
        "org.tajiro.policy.mapper",
        "org.tajiro.finance.mapper",
        "org.tajiro.seller.mapper"
})
@ComponentScan(basePackages = {
        "org.tajiro.auth.service",
        "org.tajiro.security.jwt",
        "org.tajiro.terms.service",
        "org.tajiro.user.service",
        "org.tajiro.property.service",
        "org.tajiro.comparison.service",
        "org.tajiro.report.service",
        "org.tajiro.policy.service",
        "org.tajiro.finance.service",
        "org.tajiro.seller.service",
        "org.tajiro.seller.event",
        "org.tajiro.location.service"
})
public class RootConfig {

    @Autowired
    private ApplicationContext applicationContext;

    @Value("${db.driver-class-name}")
    private String driverClassName;

    @Value("${db.url}")
    private String url;

    @Value("${db.username}")
    private String username;

    @Value("${db.password}")
    private String password;

    @Bean(destroyMethod = "close")
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(driverClassName);
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setPoolName("tajiro-hikari-pool");
        // [스켈레톤 편의 추가] 참고 프로젝트(backend/)에는 없는 한 줄입니다.
        // 이게 없으면 MySQL이 떠 있지 않을 때 커넥션 검사에 실패해 서버가 아예 기동되지 않습니다.
        // DB 세팅을 마쳤다면 지우세요.
        config.setInitializationFailTimeout(-1);
        return new HikariDataSource(config);
    }

    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setConfigLocation(
                applicationContext.getResource("classpath:/mybatis-config.xml")
        );
        // Mapper XML 위치: src/main/resources/org/tajiro/{domain}/mapper/XxxMapper.xml
        factoryBean.setMapperLocations(
                applicationContext.getResources("classpath*:/org/tajiro/**/mapper/*Mapper.xml")
        );
        factoryBean.setDataSource(dataSource);
        return factoryBean.getObject();
    }

    @Bean
    public DataSourceTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
