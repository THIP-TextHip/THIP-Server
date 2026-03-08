package konkuk.thip.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement(order = Ordered.LOWEST_PRECEDENCE - 1)
public class AppConfig {
}
