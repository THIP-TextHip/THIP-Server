package konkuk.thip.feed.adapter.out.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.BookJpaRepository;
import konkuk.thip.feed.adapter.out.persistence.FeedJpaRepository;
import konkuk.thip.user.adapter.out.jpa.AliasJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserRole;
import konkuk.thip.user.adapter.out.persistence.AliasJpaRepository;
import konkuk.thip.user.adapter.out.persistence.UserJpaRepository;
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
class FeedJpaEntityTest {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private UserJpaRepository userRepository;

    @Autowired
    private AliasJpaRepository aliasRepository;

    @Autowired
    private BookJpaRepository bookRepository;

    @Autowired
    private FeedJpaRepository feedRepository;

    private UserJpaEntity createUser() {
        AliasJpaEntity alias = AliasJpaEntity.builder()
                .value("칭호")
                .imageUrl("test-image-url")
                .color("red")
                .build();

        aliasRepository.save(alias);

        UserJpaEntity user = UserJpaEntity.builder()
                .email("test@test.com")
                .nickname("테스터")
                .imageUrl("https://test.img")
                .aliasForUserJpaEntity(alias)
                .role(UserRole.USER)
                .build();
        return userRepository.save(user);
    }

    private BookJpaEntity createBook() {
        return bookRepository.save(BookJpaEntity.builder()
                .title("책제목")
                .authorName("저자")
                .isbn("isbn")
                .bestSeller(false)
                .publisher("출판사")
                .imageUrl("img")
                .pageCount(100)
                .description("설명")
                .build());
    }

    @Test
    @DisplayName("FeedJpaEntity 저장 및 조회 테스트")
    void saveAndFindFeed() {
        UserJpaEntity user = createUser();
        BookJpaEntity book = createBook();

        FeedJpaEntity feed = FeedJpaEntity.builder()
                .content("피드 내용")
                .userJpaEntity(user)
                .isPublic(true)
                .bookJpaEntity(book)
                .build();

        feedRepository.save(feed);
        em.flush();
        em.clear();

        FeedJpaEntity found = feedRepository.findById(feed.getPostId()).orElseThrow();
        assertThat(found).isNotNull();
        assertThat(found.getContent()).isEqualTo("피드 내용");
        assertThat(found.getBookJpaEntity().getTitle()).isEqualTo("책제목");
    }
}