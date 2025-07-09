package konkuk.thip.record.adapter.out.persistence;

import jakarta.persistence.EntityManager;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.record.adapter.out.jpa.RecordJpaEntity;
import konkuk.thip.room.adapter.out.jpa.CategoryJpaEntity;
import konkuk.thip.room.adapter.out.jpa.RoomJpaEntity;
import konkuk.thip.user.adapter.out.jpa.AliasJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(konkuk.thip.config.TestQuerydslConfig.class)
@DisplayName("[JPA] RecordQueryRepositoryImpl 테스트")
class RecordQueryRepositoryImplTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private RecordJpaRepository recordJpaRepository;

    @Autowired
    RecordQueryRepositoryImpl recordQueryRepository;

    @Test
    @DisplayName("mine 타입일 경우 유저 ID에 해당하는 기록만 조회된다")
    void testFindRecordsByRoom_mine() {
        // given
        AliasJpaEntity alias = TestEntityFactory.createAlias();
        em.persist(alias);

        UserJpaEntity user1 = TestEntityFactory.createUser(alias);
        UserJpaEntity user2 = TestEntityFactory.createUser(alias);
        em.persist(user1);
        em.persist(user2);

        BookJpaEntity book = TestEntityFactory.createBook();
        em.persist(book);

        CategoryJpaEntity category = TestEntityFactory.createCategory(alias);
        em.persist(category);

        RoomJpaEntity room = TestEntityFactory.createRoom(book, category);
        em.persist(room);

        RecordJpaEntity r1 = RecordJpaEntity.builder()
                .userJpaEntity(user1)
                .roomJpaEntity(room)
                .content("user1의 레코드")
                .page(1)
                .isOverview(false)
                .build();

        RecordJpaEntity r2 = RecordJpaEntity.builder()
                .userJpaEntity(user2)
                .roomJpaEntity(room)
                .content("user2의 레코드")
                .page(1)
                .isOverview(false)
                .build();

        em.persist(r1);
        em.persist(r2);
        em.flush();
        em.clear();

        // when
        List<RecordJpaEntity> result = recordQueryRepository.findRecordsByRoom(
                room.getRoomId(),
                "mine",
                1,
                1,
                user1.getUserId()
        );

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserJpaEntity().getUserId()).isEqualTo(user1.getUserId());
        assertThat(result.get(0).getContent()).isEqualTo("user1의 레코드");
    }

    @Test
    @DisplayName("pageStart, pageEnd가 null이면 isOverview가 true인 레코드만 조회된다")
    void testFindRecordsByRoom_overview() {
        // given
        AliasJpaEntity alias = TestEntityFactory.createAlias();
        em.persist(alias);

        UserJpaEntity user = TestEntityFactory.createUser(alias);
        em.persist(user);

        BookJpaEntity book = TestEntityFactory.createBook();
        em.persist(book);

        CategoryJpaEntity category = TestEntityFactory.createCategory(alias);
        em.persist(category);

        RoomJpaEntity room = TestEntityFactory.createRoom(book, category);
        em.persist(room);

        RecordJpaEntity overview = RecordJpaEntity.builder()
                .userJpaEntity(user)
                .roomJpaEntity(room)
                .content("요약 레코드")
                .isOverview(true)
                .build();

        RecordJpaEntity normal = RecordJpaEntity.builder()
                .userJpaEntity(user)
                .roomJpaEntity(room)
                .content("일반 레코드")
                .isOverview(false)
                .build();

        em.persist(overview);
        em.persist(normal);
        em.flush();
        em.clear();

        // when
        List<RecordJpaEntity> result = recordQueryRepository.findRecordsByRoom(
                room.getRoomId(),
                "group",
                null,
                null,
                user.getUserId()
        );

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).isOverview()).isTrue();
    }
}