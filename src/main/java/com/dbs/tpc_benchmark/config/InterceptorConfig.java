package com.dbs.tpc_benchmark.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.lang.NonNull;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {
    @Autowired
    private JWTinterceptor jwtinterceptor;

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(jwtinterceptor)
        .addPathPatterns("/**")
        .excludePathPatterns("/users/login", "/users/register");
    }
}
