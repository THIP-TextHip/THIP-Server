package konkuk.thip.config;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class WorkerThreadConfig implements AsyncConfigurer {

    /**
     * FCM 푸시알림 전용 실행기
     * - 이벤트 리스너들의 알림 발송 처리
     * - 빈번한 짧은 I/O 작업 최적화
     */
    @Bean(name = "fcmAsyncExecutor")
    public Executor fcmAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(6);        // FCM 알림 기본 처리량
        executor.setMaxPoolSize(15);        // 알림 급증 시 확장
        executor.setQueueCapacity(100);     // 알림 대기 큐 (적당한 크기)
        executor.setThreadNamePrefix("fcm-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
//        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }

    /**
     * 스케줄러 배치작업 전용 실행기
     * - 데이터 정리, 방 상태 변경 등 배치 작업
     * - 긴 실행시간 작업 최적화
     */
    @Bean(name = "schedulerAsyncExecutor")
    public Executor schedulerAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);        // 배치작업은 적은 동시 실행
        executor.setMaxPoolSize(4);         // 최대 확장도 제한적
        executor.setQueueCapacity(10);      // 작은 큐 (스케줄링된 작업이므로)
        executor.setThreadNamePrefix("scheduler-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
//        executor.setAwaitTerminationSeconds(120);  // 배치작업 완료 대기시간
        executor.initialize();
        return executor;
    }

    @Override
    public Executor getAsyncExecutor() {
        return fcmAsyncExecutor(); // 기본은 FCM 풀 사용
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new SimpleAsyncUncaughtExceptionHandler();
    }
}