package konkuk.thip.user.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class User {

    /**
     * 도메인 엔티티 -> 비즈니스 로직 수행
     */

    private Long id;

    private String name;

    private String email;

    private String password;

    public static User withoutId(String name, String email, String password) {
        return User.builder()
                .id(null)
                .name(name)
                .email(email)
                .password(password)
                .build();
    }
}
