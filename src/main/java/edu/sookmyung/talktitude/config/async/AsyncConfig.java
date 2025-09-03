package edu.sookmyung.talktitude.config.async;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class AsyncConfig {
    // 필요 시 ThreadPoolTaskExecutor 커스터마이즈 가능
}
