package konkuk.thip.common.security.oauth2;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public class CustomOAuth2User implements OAuth2User {

    private final LoginUser loginUser;

    @Override
    public Map<String, Object> getAttributes() {
        return null;
    }

    @Override
    public String getName() {
        return loginUser.oauth2Id();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return "NORMAL_USER"; // 임시 사용자 권한
            }
        });
        return authorities;
    }

    public String getOauth2Id() {
        return loginUser.oauth2Id();
    }

    public boolean isNewUser() {
        return loginUser.isNewUser();
    }

}
