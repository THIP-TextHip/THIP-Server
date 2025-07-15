package konkuk.thip.room.adapter.out.jpa;

import jakarta.persistence.EntityManager;
import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.record.adapter.out.jpa.RecordJpaEntity;
import konkuk.thip.record.adapter.out.persistence.repository.RecordJpaRepository;
import konkuk.thip.room.adapter.out.persistence.repository.CategoryJpaRepository;
import konkuk.thip.room.adapter.out.persistence.repository.RoomJpaRepository;
import konkuk.thip.user.adapter.out.jpa.AliasJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.AliasJpaRepository;
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
@DisplayName("[JPA] RecordJpaEntity 테스트")
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

    @Test
    @DisplayName("RecordJpaEntity 저장 및 조회 테스트")
    void saveAndFindRecord() {
        // given
        AliasJpaEntity alias = aliasRepository.save(TestEntityFactory.createLiteratureAlias());
        UserJpaEntity user = userRepository.save(TestEntityFactory.createUser(alias));
        BookJpaEntity book = bookRepository.save(TestEntityFactory.createBook());
        CategoryJpaEntity category = categoryRepository.save(TestEntityFactory.createLiteratureCategory(alias));
        RoomJpaEntity room = roomRepository.save(TestEntityFactory.createRoom(book, category));
        RecordJpaEntity record = recordRepository.save(TestEntityFactory.createRecord(user, room));

        em.flush();
        em.clear();

        // when
        RecordJpaEntity found = recordRepository.findById(record.getPostId()).orElseThrow();

        // then
        assertThat(found).isNotNull();
        assertThat(found.getPage()).isEqualTo(22);
        assertThat(found.getRoomJpaEntity().getTitle()).isEqualTo("방이름");
        assertThat(found.getUserJpaEntity().getNickname()).isEqualTo("테스터");
    }
}