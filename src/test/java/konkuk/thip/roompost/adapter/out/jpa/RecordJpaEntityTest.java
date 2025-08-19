package konkuk.thip.roompost.adapter.out.jpa;

import jakarta.persistence.EntityManager;
import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.room.adapter.out.jpa.RoomJpaEntity;
import konkuk.thip.room.domain.Category;
import konkuk.thip.roompost.adapter.out.persistence.repository.record.RecordJpaRepository;
import konkuk.thip.room.adapter.out.persistence.repository.RoomJpaRepository;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.user.domain.Alias;
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
    private BookJpaRepository bookRepository;

    @Autowired
    private RoomJpaRepository roomRepository;

    @Autowired
    private RecordJpaRepository recordRepository;

    @Test
    @DisplayName("RecordJpaEntity 저장 및 조회 테스트")
    void saveAndFindRecord() {
        // given
        Alias alias = TestEntityFactory.createLiteratureAlias();
        UserJpaEntity user = userRepository.save(TestEntityFactory.createUser(alias));
        BookJpaEntity book = bookRepository.save(TestEntityFactory.createBook());
        Category category = TestEntityFactory.createLiteratureCategory();
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