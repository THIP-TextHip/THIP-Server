package konkuk.thip.user.adapter.out.mapper;

import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.domain.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    /**
     * domain -> jpa entity 로 mapping
     * id 값은 auto generate 이므로 설정 X
     */
    public UserJpaEntity toJpaEntity(User user) {
        return UserJpaEntity.builder()
                .name(user.getName())
                .email(user.getEmail())
                .password(user.getPassword())
                .build();
    }

    public User toDomainEntity(UserJpaEntity jpaEntity) {
        return User.builder()
                .id(jpaEntity.getId())
                .name(jpaEntity.getName())
                .email(jpaEntity.getEmail())
                .password(jpaEntity.getPassword())
                .build();
    }
}
