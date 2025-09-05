package konkuk.thip.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;

import java.util.concurrent.Executor;

// 테스트용: 동기 실행 강제
@Configuration
@Profile("test")
public class TestAsyncConfig implements AsyncConfigurer {

    @Bean(name = "fcmAsyncExecutor")
    public Executor fcmAsyncExecutor() {
        return new SyncTaskExecutor();
    }

    @Bean(name = "schedulerAsyncExecutor")
    public Executor schedulerAsyncExecutor() {
        return new SyncTaskExecutor();
    }

    @Override
    public Executor getAsyncExecutor() {
        return new SyncTaskExecutor();
    }
}