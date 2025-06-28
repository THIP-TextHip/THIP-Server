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
                .nickname(user.getNickname())
                .imageUrl(user.getImageUrl())
                .role(UserRole.from(user.getUserRole()))
                .oauth2Id(user.getOauth2Id())
                .aliasForUserJpaEntity(aliasJpaEntity)
                .build();
    }

    public User toDomainEntity(UserJpaEntity userJpaEntity) {
        return User.builder()
                .id(userJpaEntity.getUserId())
                .nickname(userJpaEntity.getNickname())
                .imageUrl(userJpaEntity.getImageUrl())
                .userRole(userJpaEntity.getRole().getType())
                .aliasId(userJpaEntity.getAliasForUserJpaEntity().getAliasId())
                .oauth2Id(userJpaEntity.getOauth2Id())
                .createdAt(userJpaEntity.getCreatedAt())
                .modifiedAt(userJpaEntity.getModifiedAt())
                .status(userJpaEntity.getStatus())
                .build();
    }
}
