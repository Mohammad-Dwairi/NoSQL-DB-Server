package com.atypon.nosqldbserver.config;

import com.atypon.nosqldbserver.interceptor.ReplicationInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Profile("master")
@Configuration
@RequiredArgsConstructor
public class DBConfig implements WebMvcConfigurer {

    private final ReplicationInterceptor replicationInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(replicationInterceptor);
    }
}
