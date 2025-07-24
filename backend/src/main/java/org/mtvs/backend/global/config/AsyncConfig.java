package org.mtvs.backend.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.core.task.TaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "riotApiExecutor")
    public TaskExecutor riotApiExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        executor.setCorePoolSize(8);        // 기본 스레드 8개 (Riot API 동시 호출용)
        executor.setMaxPoolSize(16);        // 최대 스레드 16개
        executor.setQueueCapacity(50);      // 대기 큐 50개
        executor.setThreadNamePrefix("RiotAPI-");
        
        executor.setKeepAliveSeconds(60);   // 유휴 스레드 60초 후 제거
        executor.setAllowCoreThreadTimeOut(true);
        
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        
        executor.setRejectedExecutionHandler((runnable, taskExecutor) -> {
            throw new RuntimeException("Riot API 스레드 풀 포화 상태: " + runnable.toString());
        });
        
        executor.initialize();
        return executor;
    }
}