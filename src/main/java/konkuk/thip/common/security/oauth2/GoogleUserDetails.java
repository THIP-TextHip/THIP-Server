package konkuk.thip.common.security.oauth2;

import java.util.Map;

public class GoogleUserDetails implements OAuth2UserDetails{

    private final Map<String, Object> attribute;

    public GoogleUserDetails(Map<String, Object> attribute) {

        this.attribute = attribute;
    }

    @Override
    public String getProvider() {

        return "google";
    }

    @Override
    public String getProviderId() {

        return attribute.get("sub").toString();
    }

    @Override
    public String getEmail() {

        return attribute.get("email").toString();
    }
}
