package konkuk.thip.domain.feed.adapter.out.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import konkuk.thip.domain.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.domain.book.adapter.out.persistence.BookJpaRepository;
import konkuk.thip.domain.feed.adapter.out.persistence.FeedJpaRepository;
import konkuk.thip.domain.user.adapter.out.jpa.AliasJpaEntity;
import konkuk.thip.domain.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.domain.user.adapter.out.jpa.UserRole;
import konkuk.thip.domain.user.adapter.out.persistence.AliasJpaRepository;
import konkuk.thip.domain.user.adapter.out.persistence.UserJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(connection= EmbeddedDatabaseConnection.H2)
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
        AliasJpaEntity alias = AliasJpaEntity.builder().value("익명1").build();
        aliasRepository.save(alias);

        UserJpaEntity user = UserJpaEntity.builder()
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