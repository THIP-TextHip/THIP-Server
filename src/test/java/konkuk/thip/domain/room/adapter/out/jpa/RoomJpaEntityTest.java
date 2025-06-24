package konkuk.thip.domain.room.adapter.out.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.room.adapter.out.jpa.CategoryJpaEntity;
import konkuk.thip.book.adapter.out.persistence.BookJpaRepository;
import konkuk.thip.room.adapter.out.persistence.CategoryJpaRepository;
import konkuk.thip.room.adapter.out.jpa.RoomJpaEntity;
import konkuk.thip.room.adapter.out.persistence.RoomJpaRepository;
import konkuk.thip.user.adapter.out.jpa.AliasJpaEntity;
import konkuk.thip.user.adapter.out.persistence.AliasJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

@DataJpaTest
@ActiveProfiles("test")
class RoomJpaEntityTest {
    @PersistenceContext
    private EntityManager em;

    @Autowired
    private BookJpaRepository bookRepository;

    @Autowired
    private AliasJpaRepository aliasRepository;

    @Autowired
    private CategoryJpaRepository categoryRepository;

    @Autowired
    private RoomJpaRepository roomRepository;

    @Test
    @DisplayName("RoomJpaEntity 저장 및 조회 테스트")
    void saveAndFindRoom() {
        //given
        BookJpaEntity book = BookJpaEntity.builder()
                .title("테스트 책")
                .authorName("테스트 저자")
                .isbn("1234567890")
                .bestSeller(false)
                .publisher("테스트 출판사")
                .imageUrl("http://test.image.url")
                .pageCount(300)
                .description("테스트 책 설명")
                .build();

        bookRepository.save(book);

        AliasJpaEntity alias = AliasJpaEntity.builder()
                .value("칭호1")
                .build();

        aliasRepository.save(alias);

        CategoryJpaEntity category = CategoryJpaEntity.builder()
                .value("카테고리1")
                .aliasForCategoryJpaEntity(alias)
                .build();

        categoryRepository.save(category);

        RoomJpaEntity room = RoomJpaEntity.builder()
                .title("테스트 방")
                .description("테스트 방 설명")
                .isPublic(true)
                .startDate(LocalDate.of(2025, 6, 20))
                .endDate(LocalDate.of(2025, 6, 30))
                .recruitCount(5)
                .bookJpaEntity(book)
                .categoryJpaEntity(category)
                .build();

        //when
        roomRepository.save(room);
        em.flush();
        em.clear();

        RoomJpaEntity foundRoom = roomRepository.findById(room.getRoomId()).orElseThrow();

        //then
        assert foundRoom.getTitle().equals("테스트 방");
        assert foundRoom.getDescription().equals("테스트 방 설명");
        assert foundRoom.isPublic();
        assert foundRoom.getStartDate().equals(LocalDate.of(2025, 6, 20));
        assert foundRoom.getEndDate().equals(LocalDate.of(2025, 6, 30));
        assert foundRoom.getRecruitCount() == 5;
        assert foundRoom.getBookJpaEntity().getTitle().equals("테스트 책");
        assert foundRoom.getBookJpaEntity().getAuthorName().equals("테스트 저자");
        assert foundRoom.getBookJpaEntity().getIsbn().equals("1234567890");
    }



}