package konkuk.thip.record.adapter.out.persistence;

import jakarta.persistence.EntityManager;
import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.config.TestQuerydslConfig;
import konkuk.thip.record.adapter.in.web.response.RecordSearchResponse;
import konkuk.thip.record.adapter.out.jpa.RecordJpaEntity;
import konkuk.thip.room.adapter.out.jpa.CategoryJpaEntity;
import konkuk.thip.room.adapter.out.jpa.RoomJpaEntity;
import konkuk.thip.user.adapter.out.jpa.AliasJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(TestQuerydslConfig.class)
@DisplayName("[JPA] RecordQueryRepositoryImpl 테스트")
class RecordQueryRepositoryImplTest {

    @Autowired
    private RecordQueryRepositoryImpl recordQueryRepository;

    @Autowired
    private EntityManager em;

    private RoomJpaEntity room;
    private UserJpaEntity user1;
    private UserJpaEntity user2;

    @BeforeEach
    void setUp() {
        AliasJpaEntity alias = TestEntityFactory.createLiteratureAlias();
        em.persist(alias);

        user1 = TestEntityFactory.createUser(alias);
        user2 = TestEntityFactory.createUser(alias);
        em.persist(user1);
        em.persist(user2);

        BookJpaEntity book = TestEntityFactory.createBook();
        em.persist(book);

        CategoryJpaEntity category = TestEntityFactory.createLiteratureCategory(alias);
        em.persist(category);

        room = TestEntityFactory.createRoom(book, category);
        em.persist(room);

        for (int i = 0; i < 10; i++) {
            RecordJpaEntity record = RecordJpaEntity.builder()
                    .userJpaEntity(i % 2 == 0 ? user1 : user2)
                    .roomJpaEntity(room)
                    .content("레코드 " + i)
                    .likeCount(1)
                    .commentCount(1)
                    .isOverview(false)
                    .page(1)
                    .build();
            em.persist(record);
        }

        for (int i = 0; i < 10; i++) {
            RecordJpaEntity record = RecordJpaEntity.builder()
                    .userJpaEntity(i % 2 == 0 ? user1 : user2)
                    .roomJpaEntity(room)
                    .content("레코드 " + i)
                    .likeCount(1)
                    .commentCount(1)
                    .isOverview(true)
                    .page(book.getPageCount()) // 총평은 책 전체 페이지로 저장
                    .build();
            em.persist(record);
        }

        em.flush();
        em.clear();
    }

//    @AfterEach
//    void tearDown() {
//        em.createQuery("DELETE FROM RecordJpaEntity").executeUpdate();
//        em.createQuery("DELETE FROM RoomJpaEntity").executeUpdate();
//        em.createQuery("DELETE FROM UserJpaEntity").executeUpdate();
//        em.createQuery("DELETE FROM AliasJpaEntity").executeUpdate();
//        em.createQuery("DELETE FROM CategoryJpaEntity").executeUpdate();
//        em.createQuery("DELETE FROM BookJpaEntity").executeUpdate();
//    }

    @Test
    @DisplayName("기본 조회 및 페이징 동작 확인")
    void test_paging() {
        Page<RecordSearchResponse.RecordSearchResult> result = recordQueryRepository.findRecordsByRoom(
                room.getRoomId(),
                "group",
                1,
                1,
                false,
                user1.getUserId(),
                PageRequest.of(0, 5)
        );

        assertThat(result.getNumberOfElements()).isEqualTo(5);
        assertThat(result.getTotalElements()).isEqualTo(10);
        assertThat(result.isLast()).isFalse();
        assertThat(result.isFirst()).isTrue();
    }

    @Test
    @DisplayName("viewType이 mine일 때 user1의 레코드만 조회된다")
    void test_viewType_mine() {
        Page<RecordSearchResponse.RecordSearchResult> result = recordQueryRepository.findRecordsByRoom(
                room.getRoomId(),
                "mine",
                1,
                1,
                false,
                user1.getUserId(),
                PageRequest.of(0, 10)
        );

        assertThat(result).allSatisfy(record ->
                assertThat(record.userId()).isEqualTo(user1.getUserId()));
        assertThat(result.getNumberOfElements()).isEqualTo(5); // user1이 작성한 레코드가 5개
    }

    @Test
    @DisplayName("latest 기준 정렬 확인")
    void test_sortingBy_latest() {
        Page<RecordSearchResponse.RecordSearchResult> result = recordQueryRepository.findRecordsByRoom(
                room.getRoomId(),
                null,
                1,
                1,
                false,
                user1.getUserId(),
                PageRequest.of(0, 10, org.springframework.data.domain.Sort.by("createdAt").descending())
        );

        List<RecordSearchResponse.RecordSearchResult> content = result.getContent();
        for (int i = 1; i < content.size(); i++) {
            assertThat(content.get(i - 1).postDate()).isGreaterThanOrEqualTo(content.get(i).postDate());
        }
    }

    @Test
    @DisplayName("isOverview가 true일 때 레코드가 총평 기록만 조회된다.")
    void test_isOverview_true() {
        Page<RecordSearchResponse.RecordSearchResult> result = recordQueryRepository.findRecordsByRoom(
                room.getRoomId(),
                "group",
                1,
                1,
                true,
                user1.getUserId(),
                PageRequest.of(0, 10)
        );

        assertThat(result.getNumberOfElements()).isEqualTo(10);
        assertThat(result.getContent()).allSatisfy(record ->
                assertThat(record.page()).isEqualTo(room.getBookJpaEntity().getPageCount()));
    }
}