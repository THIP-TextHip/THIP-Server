package konkuk.thip.user.adapter.out.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.user.adapter.out.persistence.repository.alias.AliasJpaRepository;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(konkuk.thip.config.TestQuerydslConfig.class)    // DataJpaTest 이므로 JPA 제외 빈 추가로 import
@DisplayName("[JPA] UserJpaEntity 테스트")
class UserJpaEntityTest {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private UserJpaRepository userRepository;

    @Autowired
    private AliasJpaRepository aliasJpaRepository;

    @Test
    @DisplayName("UserJpaEntity 저장 및 조회 테스트")
    void saveAndFindUser() {
        // given
        AliasJpaEntity alias = aliasJpaRepository.save(TestEntityFactory.createLiteratureAlias());
        UserJpaEntity user = userRepository.save(TestEntityFactory.createUser(alias));

        // when
        em.flush();
        em.clear();

        UserJpaEntity foundUser = userRepository.findById(user.getUserId()).orElseThrow();

        // then
        assertThat(foundUser.getNickname()).isEqualTo("테스터");
        assertThat(foundUser.getAliasForUserJpaEntity().getValue()).isEqualTo("문학가");
        assertThat(foundUser.getRole()).isEqualTo(UserRole.USER);
    }
}