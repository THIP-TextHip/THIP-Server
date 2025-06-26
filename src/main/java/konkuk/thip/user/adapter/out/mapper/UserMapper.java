package konkuk.thip.user.adapter.out.mapper;

import konkuk.thip.user.adapter.out.jpa.AliasJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserRole;
import konkuk.thip.user.domain.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserJpaEntity toJpaEntity(User user, AliasJpaEntity aliasJpaEntity) {
        return UserJpaEntity.builder()
                .email(user.getEmail())
                .nickname(user.getNickname())
                .imageUrl(user.getImageUrl())
                .role(UserRole.from(user.getUserRole()))
                .aliasForUserJpaEntity(aliasJpaEntity)
                .build();
    }

    public User toDomainEntity(UserJpaEntity userJpaEntity) {
        return User.builder()
                .id(userJpaEntity.getUserId())
                .email(userJpaEntity.getEmail())
                .nickname(userJpaEntity.getNickname())
                .imageUrl(userJpaEntity.getImageUrl())
                .userRole(userJpaEntity.getRole().getType())
                .aliasId(userJpaEntity.getAliasForUserJpaEntity().getAliasId())
                .createdAt(userJpaEntity.getCreatedAt())
                .modifiedAt(userJpaEntity.getModifiedAt())
                .status(userJpaEntity.getStatus())
                .build();
    }
}
