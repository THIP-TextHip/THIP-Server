package konkuk.thip.user.adapter.out.persistence;

import konkuk.thip.common.exception.EntityNotFoundException;
import konkuk.thip.user.adapter.out.jpa.AliasJpaEntity;
import konkuk.thip.user.adapter.out.mapper.AliasMapper;
import konkuk.thip.user.application.port.out.AliasCommandPort;
import konkuk.thip.user.domain.Alias;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import static konkuk.thip.common.exception.code.ErrorCode.ALIAS_NOT_FOUND;

@Repository
@RequiredArgsConstructor
public class AliasCommandPersistenceAdapter implements AliasCommandPort {

    private final AliasMapper aliasMapper;
    private final AliasJpaRepository aliasJpaRepository;

    @Override
    public Alias findById(Long aliasId) {
        AliasJpaEntity aliasJpaEntity = aliasJpaRepository.findById(aliasId).orElseThrow(
                () -> new EntityNotFoundException(ALIAS_NOT_FOUND));

        return aliasMapper.toDomainEntity(aliasJpaEntity);
    }
}
