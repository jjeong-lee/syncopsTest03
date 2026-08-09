package kr.ac.knue.facultyassessment.auth;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "app.foundation.enabled", havingValue = "true", matchIfMissing = true)
public class AuthenticationConfiguration {

    @Bean
    FilterRegistrationBean<SessionAuthorizationFilter> sessionAuthorizationFilterRegistration(SessionAuthorizationFilter filter) {
        FilterRegistrationBean<SessionAuthorizationFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(filter);
        registration.addUrlPatterns("/api/*");
        registration.setOrder(1);
        return registration;
    }
}
