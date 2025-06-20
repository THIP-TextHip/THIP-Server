package konkuk.thip.domain.user.adapter.out.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import konkuk.thip.domain.user.adapter.out.persistence.AliasJpaRepository;
import konkuk.thip.domain.user.adapter.out.persistence.UserJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(connection= EmbeddedDatabaseConnection.H2)
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
        AliasJpaEntity alias = AliasJpaEntity.builder()
                .value("칭호")
                .build();
        aliasJpaRepository.save(alias);

        UserJpaEntity user = UserJpaEntity.builder()
                .nickname("테스트유저")
                .imageUrl("http://image.url")
                .role(UserRole.USER)
                .aliasForUserJpaEntity(alias)
                .build();

        // when
        userRepository.save(user);
        em.flush();
        em.clear();

        UserJpaEntity foundUser = userRepository.findById(user.getUserId()).orElseThrow();

        // then
        assertThat(foundUser.getNickname()).isEqualTo("테스트유저");
        assertThat(foundUser.getAliasForUserJpaEntity().getValue()).isEqualTo("칭호");
        assertThat(foundUser.getRole()).isEqualTo(UserRole.USER);
    }
}