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
        UserJpaEntity jpaEntity = userMapper.toJpaEntity(user);

        /**
         * domain -> jpa entity 매핑 후, 변경사항을 jpa entity에 반영
         * Spring Data JPA 의 변경감지 기능 활용 가능 (대신 해당 update 메서드가 @Transactional 범위 내에 존재해야함)
         * or 그냥 DB update 쿼리를 명시적으로 날려도 됨
         */
        // jpaEntity.changeName(String name);
        // japEntity.changePassword(String password);
        // jpaRepository.update(jpaEntity);     -> 선택 사항
    }
}
