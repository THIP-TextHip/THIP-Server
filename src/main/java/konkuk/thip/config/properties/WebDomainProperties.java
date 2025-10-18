package konkuk.thip.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Getter
@Component
@ConfigurationProperties(prefix = "server")
public class WebDomainProperties {

    private final List<String> webDomainUrls = new ArrayList<>();

    @Setter
    private String profile;

    public boolean isAllowed(String target) {
        if (target == null || target.isBlank()) {
            return false;
        }

        // 운영환경이 아닐 경우: target이 비어있지 않다면 전부 허용
        if (!"prod".equals(profile)) {
            return true;
        }

        try {
            URI uri = URI.create(target);
            String origin = uri.getScheme() + "://" + uri.getHost() + (uri.getPort() > 0 ? ":" + uri.getPort() : "");
            return webDomainUrls.stream().anyMatch(o -> o.equalsIgnoreCase(origin));
        } catch (Exception e) {
            return false;
        }
    }
}
