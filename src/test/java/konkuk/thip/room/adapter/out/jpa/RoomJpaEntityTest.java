package konkuk.thip.room.adapter.out.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.room.adapter.out.persistence.CategoryJpaRepository;
import konkuk.thip.room.adapter.out.persistence.RoomJpaRepository;
import konkuk.thip.user.adapter.out.jpa.AliasJpaEntity;
import konkuk.thip.book.adapter.out.persistence.BookJpaRepository;
import konkuk.thip.user.adapter.out.persistence.AliasJpaRepository;
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
@DisplayName("[JPA] RoomJpaEntity 테스트")
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
        // given
        BookJpaEntity book = bookRepository.save(TestEntityFactory.createBook());
        AliasJpaEntity alias = aliasRepository.save(TestEntityFactory.createLiteratureAlias());
        CategoryJpaEntity category = categoryRepository.save(TestEntityFactory.createLiteratureCategory(alias));
        RoomJpaEntity room = roomRepository.save(TestEntityFactory.createRoom(book, category));

        // when
        em.flush();
        em.clear();

        RoomJpaEntity foundRoom = roomRepository.findById(room.getRoomId()).orElseThrow();

        // then
        assertThat(foundRoom.getTitle()).isEqualTo("방이름");
        assertThat(foundRoom.getDescription()).isEqualTo("설명");
        assertThat(foundRoom.isPublic()).isTrue();
        assertThat(foundRoom.getRecruitCount()).isEqualTo(3);
        assertThat(foundRoom.getBookJpaEntity().getTitle()).isEqualTo("책제목");
        assertThat(foundRoom.getBookJpaEntity().getAuthorName()).isEqualTo("저자");
        assertThat(foundRoom.getBookJpaEntity().getIsbn()).isEqualTo("isbn");
    }
}