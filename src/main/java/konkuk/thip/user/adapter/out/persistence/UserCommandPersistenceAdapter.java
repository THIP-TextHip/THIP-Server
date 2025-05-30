package konkuk.thip.user.adapter.out.persistence;

import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.mapper.UserMapper;
import konkuk.thip.user.application.port.out.UserCommandPort;
import konkuk.thip.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserCommandPersistenceAdapter implements UserCommandPort {

    private final UserJpaRepository jpaRepository;
    private final UserMapper userMapper;

    @Override
    public Long save(User user) {
        UserJpaEntity jpaEntity = userMapper.toJpaEntity(user);
        return jpaRepository.save(jpaEntity).getId();
    }

    @Override
    public void update(User user) {
        userMapper.toJpaEntity(user);

        /**
         * domain -> jpa entity 로 mapping 만 하여도 jpa 변경감지 기능으로 인해 DB에 update 쿼리가 날라가긴함
         * (대신 Service 메서드에 @Transactional 어노테이션이 명시되어 있어야함)
         *
         * 아니면 그냥 명시적으로 update 쿼리를 날려도 괜춘
         */
    }
}
