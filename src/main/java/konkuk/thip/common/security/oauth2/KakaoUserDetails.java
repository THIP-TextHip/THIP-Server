package konkuk.thip.common.security.oauth2;

import java.util.Map;

import static konkuk.thip.common.security.constant.AuthParameters.KAKAO;
import static konkuk.thip.common.security.constant.AuthParameters.KAKAO_PROVIDER_ID_KEY;

public class KakaoUserDetails implements OAuth2UserDetails {

    private final Map<String, Object> attributes;

    public KakaoUserDetails(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getProvider() {
        return KAKAO.getValue();
    }

    @Override
    public String getProviderId() {
        return attributes.get(KAKAO_PROVIDER_ID_KEY.getValue()).toString();
    }

//    @Override
//    public String getEmail() {
//        Object object = attributes.get("kakao_account");
//        LinkedHashMap accountMap = (LinkedHashMap) object;
//        return accountMap.get("email").toString();
//    }
}
