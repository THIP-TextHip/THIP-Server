package konkuk.thip.room.adapter.out.jpa;

import jakarta.persistence.EntityManager;
import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.room.adapter.out.persistence.repository.CategoryJpaRepository;
import konkuk.thip.room.adapter.out.persistence.repository.RoomJpaRepository;
import konkuk.thip.user.adapter.out.jpa.AliasJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.AliasJpaRepository;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.vote.adapter.out.jpa.VoteJpaEntity;
import konkuk.thip.vote.adapter.out.persistence.repository.VoteJpaRepository;
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
@DisplayName("[JPA] VoteJpaEntity 테스트")
class VoteJpaEntityTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private UserJpaRepository userRepository;

    @Autowired
    private AliasJpaRepository aliasRepository;

    @Autowired
    private BookJpaRepository bookRepository;

    @Autowired
    private RoomJpaRepository roomRepository;

    @Autowired
    private VoteJpaRepository voteRepository;

    @Autowired
    private CategoryJpaRepository categoryRepository;

    @Test
    @DisplayName("VoteJpaEntity 저장 및 조회 테스트")
    void saveAndFindVote() {
        // given
        AliasJpaEntity alias = aliasRepository.save(TestEntityFactory.createLiteratureAlias());
        UserJpaEntity user = userRepository.save(TestEntityFactory.createUser(alias));
        BookJpaEntity book = bookRepository.save(TestEntityFactory.createBook());
        CategoryJpaEntity category = categoryRepository.save(TestEntityFactory.createLiteratureCategory(alias));
        RoomJpaEntity room = roomRepository.save(TestEntityFactory.createRoom(book, category));

        VoteJpaEntity vote = voteRepository.save(VoteJpaEntity.builder()
                .content("투표 내용")
                .userJpaEntity(user)
                .page(10)
                .isOverview(true)
                .roomJpaEntity(room)
                .build());

        em.flush();
        em.clear();

        // when
        VoteJpaEntity found = voteRepository.findById(vote.getPostId()).orElseThrow();

        // then
        assertThat(found).isNotNull();
        assertThat(found.getPage()).isEqualTo(10);
        assertThat(found.getContent()).isEqualTo("투표 내용");
        assertThat(found.getUserJpaEntity().getNickname()).isEqualTo("테스터");
        assertThat(found.getRoomJpaEntity().getTitle()).isEqualTo("방이름");
    }
}