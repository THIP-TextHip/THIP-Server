package konkuk.thip.common.security.oauth2;

import java.util.LinkedHashMap;
import java.util.Map;

public class KakaoUserDetails implements OAuth2UserDetails {

    private Map<String, Object> attributes;

    public KakaoUserDetails(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getProvider() {
        return "kakao";
    }

    @Override
    public String getProviderId() {
        return attributes.get("id").toString();
    }

//    @Override
//    public String getEmail() {
//        Object object = attributes.get("kakao_account");
//        LinkedHashMap accountMap = (LinkedHashMap) object;
//        return accountMap.get("email").toString();
//    }
}
