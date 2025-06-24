package konkuk.thip.user.adapter.out.mapper;

import konkuk.thip.user.adapter.out.jpa.AliasJpaEntity;
import konkuk.thip.user.domain.Alias;
import org.springframework.stereotype.Component;

@Component
public class AliasMapper {

    public AliasJpaEntity toJpaEntity(Alias alias) {
        return AliasJpaEntity.builder()
                .value(alias.getValue())
                .build();
    }

    public Alias toDomainEntity(AliasJpaEntity aliasJpaEntity) {
        return Alias.builder()
                .id(aliasJpaEntity.getAliasId())
                .value(aliasJpaEntity.getValue())
                .createdAt(aliasJpaEntity.getCreatedAt())
                .modifiedAt(aliasJpaEntity.getModifiedAt())
                .status(aliasJpaEntity.getStatus())
                .build();
    }
}
