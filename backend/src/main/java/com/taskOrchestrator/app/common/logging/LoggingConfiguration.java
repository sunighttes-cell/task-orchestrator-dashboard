package com.taskOrchestrator.app.common.logging;

import com.taskOrchestrator.app.auth.application.CurrentUserProvider;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class LoggingConfiguration {

    @Bean
    public FilterRegistrationBean<RequestCorrelationFilter>
    requestCorrelationFilter(CurrentUserProvider currentUserProvider) {
        FilterRegistrationBean<RequestCorrelationFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RequestCorrelationFilter(currentUserProvider));
        registration.addUrlPatterns("/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);

        return registration;
    }
}