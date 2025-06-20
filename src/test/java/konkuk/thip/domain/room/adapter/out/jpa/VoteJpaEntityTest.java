package konkuk.thip.domain.room.adapter.out.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import konkuk.thip.domain.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.domain.book.adapter.out.persistence.BookJpaRepository;
import konkuk.thip.domain.room.adapter.out.persistence.RoomJpaRepository;
import konkuk.thip.domain.room.adapter.out.persistence.VoteJpaRepository;
import konkuk.thip.domain.user.adapter.out.jpa.AliasJpaEntity;
import konkuk.thip.domain.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.domain.user.adapter.out.jpa.UserRole;
import konkuk.thip.domain.user.adapter.out.persistence.AliasJpaRepository;
import konkuk.thip.domain.user.adapter.out.persistence.UserJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class VoteJpaEntityTest {

    @PersistenceContext
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

    private RoomJpaEntity createRoom(BookJpaEntity book) {
        return roomRepository.save(RoomJpaEntity.builder()
                .title("방이름")
                .description("설명")
                .isPublic(true)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(5))
                .recruitCount(3)
                .bookJpaEntity(book)
                .build());
    }

    @Test
    @DisplayName("VoteJpaEntity 저장 및 조회 테스트")
    void saveAndFindVote() {
        UserJpaEntity user = createUser();
        RoomJpaEntity room = createRoom(createBook());

        VoteJpaEntity vote = VoteJpaEntity.builder()
                .content("투표 내용")
                .userJpaEntity(user)
                .page(10)
                .isOverview(true)
                .roomJpaEntity(room)
                .build();

        voteRepository.save(vote);
        em.flush();
        em.clear();

        VoteJpaEntity found = voteRepository.findById(vote.getPostId()).orElseThrow();
        assertThat(found).isNotNull();
        assertThat(found.getPage()).isEqualTo(10);
        assertThat(found.getRoomJpaEntity().getTitle()).isEqualTo("방이름");
    }
}