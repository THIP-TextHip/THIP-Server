package konkuk.thip.domain.room.adapter.out.jpa;

import jakarta.persistence.EntityManager;
import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.BookJpaRepository;
import konkuk.thip.record.adapter.out.jpa.RecordJpaEntity;
import konkuk.thip.record.adapter.out.persistence.RecordJpaRepository;
import konkuk.thip.room.adapter.out.jpa.CategoryJpaEntity;
import konkuk.thip.room.adapter.out.jpa.RoomJpaEntity;
import konkuk.thip.room.adapter.out.persistence.CategoryJpaRepository;
import konkuk.thip.room.adapter.out.persistence.RoomJpaRepository;
import konkuk.thip.user.adapter.out.jpa.AliasJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserRole;
import konkuk.thip.user.adapter.out.persistence.AliasJpaRepository;
import konkuk.thip.user.adapter.out.persistence.UserJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class RecordJpaEntityTest {

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
    private RecordJpaRepository recordRepository;

    @Autowired
    private CategoryJpaRepository categoryRepository;

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

    private RoomJpaEntity createRoom(BookJpaEntity book, CategoryJpaEntity category) {
        return roomRepository.save(RoomJpaEntity.builder()
                .title("방이름")
                .description("설명")
                .isPublic(true)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(5))
                .recruitCount(3)
                .bookJpaEntity(book)
                .categoryJpaEntity(category)
                .build());
    }

    private CategoryJpaEntity createCategory() {
        AliasJpaEntity alias = AliasJpaEntity.builder().value("익명1").build();
        aliasRepository.save(alias);

        return categoryRepository.save(CategoryJpaEntity.builder()
                        .value("카테고리1")
                        .aliasForCategoryJpaEntity(alias)
                        .build());
    }

    @Test
    @DisplayName("RecordJpaEntity 저장 및 조회 테스트")
    void saveAndFindRecord() {
        UserJpaEntity user = createUser();
        RoomJpaEntity room = createRoom(createBook(), createCategory());

        RecordJpaEntity record = RecordJpaEntity.builder()
                .content("기록 내용")
                .userJpaEntity(user)
                .page(22)
                .isOverview(false)
                .roomJpaEntity(room)
                .build();

        recordRepository.save(record);
        em.flush();
        em.clear();

        RecordJpaEntity found = recordRepository.findById(record.getPostId()).orElseThrow();
        assertThat(found).isNotNull();
        assertThat(found.getPage()).isEqualTo(22);
        assertThat(found.getRoomJpaEntity().getTitle()).isEqualTo("방이름");
    }
}