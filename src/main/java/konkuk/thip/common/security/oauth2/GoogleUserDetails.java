package konkuk.thip.common.security.oauth2;

import java.util.Map;

import static konkuk.thip.common.security.constant.AuthParameters.GOOGLE;
import static konkuk.thip.common.security.constant.AuthParameters.GOOGLE_PROVIDER_ID_KEY;

public class GoogleUserDetails implements OAuth2UserDetails{

    private final Map<String, Object> attribute;

    public GoogleUserDetails(Map<String, Object> attribute) {

        this.attribute = attribute;
    }

    @Override
    public String getProvider() {

        return GOOGLE.getValue();
    }

    @Override
    public String getProviderId() {

        return attribute.get(GOOGLE_PROVIDER_ID_KEY.getValue()).toString();
    }

//    @Override
//    public String getEmail() {
//
//        return attribute.get("email").toString();
//    }
}
