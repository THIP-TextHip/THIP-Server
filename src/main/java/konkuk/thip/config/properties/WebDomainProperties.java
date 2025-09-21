package konkuk.thip.config.properties;

import lombok.Getter;
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

    public boolean isAllowed(String target) {
        try {
            URI uri = URI.create(target);
            String origin = uri.getScheme() + "://" + uri.getHost() + (uri.getPort() > 0 ? ":" + uri.getPort() : "");
            return webDomainUrls.stream().anyMatch(o -> o.equalsIgnoreCase(origin));
        } catch (Exception e) {
            return false;
        }
    }
}
