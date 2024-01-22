package com.example.jokemicroservice.config;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Slf4j
@Configuration
public class AppConfig {
    @Value("${max-pool-size}")
    private int maxPoolSize;

    @Bean
    public ThreadPoolTaskExecutor customTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor() {
            @Override
            public void shutdown() {
                super.shutdown();
                log.info("ThreadPool shutdown");
            }
        };
        executor.setMaxPoolSize(maxPoolSize);
        executor.setThreadNamePrefix("customThread-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        return executor;

    }

}
