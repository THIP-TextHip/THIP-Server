package konkuk.thip.feed.adapter.out.jpa;

import jakarta.persistence.EntityManager;
import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.feed.adapter.out.persistence.repository.FeedJpaRepository;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
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
@DisplayName("[JPA] FeedJpaEntity 테스트")
@Import(konkuk.thip.config.TestQuerydslConfig.class)    // DataJpaTest 이므로 JPA 제외 빈 추가로 import
class FeedJpaEntityTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private UserJpaRepository userRepository;

    @Autowired
    private AliasJpaRepository aliasRepository;

    @Autowired
    private BookJpaRepository bookRepository;

    @Autowired
    private FeedJpaRepository feedRepository;

    @Test
    @DisplayName("FeedJpaEntity 저장 및 조회 테스트")
    void saveAndFindFeed() {
        // given
        AliasJpaEntity alias = aliasRepository.save(TestEntityFactory.createLiteratureAlias());
        UserJpaEntity user = userRepository.save(TestEntityFactory.createUser(alias));
        BookJpaEntity book = bookRepository.save(TestEntityFactory.createBook());

        FeedJpaEntity feed = feedRepository.save(FeedJpaEntity.builder()
                .content("피드 내용")
                .userJpaEntity(user)
                .isPublic(true)
                .bookJpaEntity(book)
                .build());

        em.flush();
        em.clear();

        // when
        FeedJpaEntity found = feedRepository.findById(feed.getPostId()).orElseThrow();

        // then
        assertThat(found).isNotNull();
        assertThat(found.getContent()).isEqualTo("피드 내용");
        assertThat(found.getUserJpaEntity().getNickname()).isEqualTo("테스터");
        assertThat(found.getBookJpaEntity().getTitle()).isEqualTo("책제목");
    }
}