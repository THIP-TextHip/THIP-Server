package konkuk.thip.user.adapter.out.mapper;

import konkuk.thip.user.adapter.out.jpa.AliasJpaEntity;
import konkuk.thip.user.domain.Alias;
import org.springframework.stereotype.Component;

@Component
public class AliasMapper {

    public AliasJpaEntity toJpaEntity(Alias alias) {
        return AliasJpaEntity.builder()
                .value(alias.getValue())
                .imageUrl(alias.getImageUrl())
                .color(alias.getColor())
                .build();
    }

    public Alias toDomainEntity(AliasJpaEntity aliasJpaEntity) {
        return Alias.builder()
                .id(aliasJpaEntity.getAliasId())
                .value(aliasJpaEntity.getValue())
                .imageUrl(aliasJpaEntity.getImageUrl())
                .color(aliasJpaEntity.getColor())
                .createdAt(aliasJpaEntity.getCreatedAt())
                .modifiedAt(aliasJpaEntity.getModifiedAt())
                .status(aliasJpaEntity.getStatus())
                .build();
    }
}
