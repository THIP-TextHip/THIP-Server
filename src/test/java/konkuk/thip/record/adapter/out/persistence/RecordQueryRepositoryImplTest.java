package konkuk.thip.record.adapter.out.persistence;

import jakarta.persistence.EntityManager;
import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.post.adapter.out.jpa.PostJpaEntity;
import konkuk.thip.record.adapter.out.jpa.RecordJpaEntity;
import konkuk.thip.room.adapter.out.jpa.CategoryJpaEntity;
import konkuk.thip.room.adapter.out.jpa.RoomJpaEntity;
import konkuk.thip.user.adapter.out.jpa.AliasJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.vote.adapter.out.jpa.VoteJpaEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RecordQueryRepositoryTest {

    @Autowired
    private RecordQueryRepositoryImpl recordQueryRepository;

    @Autowired
    private EntityManager em;

    private RoomJpaEntity testRoom;
    private UserJpaEntity testUser;

    @BeforeEach
    void setUp() {
        AliasJpaEntity alias = TestEntityFactory.createAlias();
        em.persist(alias);

        testUser = TestEntityFactory.createUser(alias);
        em.persist(testUser);

        BookJpaEntity book = TestEntityFactory.createBook();
        em.persist(book);

        CategoryJpaEntity category = TestEntityFactory.createCategory(alias);
        em.persist(category);

        testRoom = TestEntityFactory.createRoom(book, category);
        em.persist(testRoom);

        em.persist(TestEntityFactory.createRecord(testUser, testRoom));
        em.persist(TestEntityFactory.createRecord(testUser, testRoom));
        em.persist(TestEntityFactory.createRecord(testUser, testRoom));

        em.persist(TestEntityFactory.createVote(testUser, testRoom));
        em.persist(TestEntityFactory.createVote(testUser, testRoom));

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("기록장 조회 - group + latest")
    void findPosts_group_latest() {
        Pageable pageable = PageRequest.of(0, 10); // page 0, size 10

        Slice<PostJpaEntity> posts = recordQueryRepository.findPostsByRoom(
                testRoom.getRoomId(), "group", "latest", 10, 50, testUser.getUserId(), pageable
        );

        assertThat(posts).isNotEmpty();
        assertThat(posts.getContent()).allMatch(p -> p instanceof RecordJpaEntity || p instanceof VoteJpaEntity);
        assertThat(posts.getContent()).allSatisfy(post -> {
            Integer page = (post instanceof RecordJpaEntity r) ? r.getPage() : ((VoteJpaEntity) post).getPage();
            assertThat(page).isBetween(10, 50);
        });
    }

    @Test
    @DisplayName("기록장 조회 - mine + comment 정렬")
    void findPosts_mine_comment() {
        Pageable pageable = PageRequest.of(0, 10);

        Slice<PostJpaEntity> posts = recordQueryRepository.findPostsByRoom(
                testRoom.getRoomId(), "mine", "comment", 10, 50, testUser.getUserId(), pageable
        );

        assertThat(posts).isNotEmpty();
        assertThat(posts.getContent()).allMatch(post -> post.getUserJpaEntity().getUserId().equals(testUser.getUserId()));
    }

    @Test
    @DisplayName("기록장 조회 - 총평만 조회 (pageStart/pageEnd = null)")
    void findPosts_overview_only() {
        Pageable pageable = PageRequest.of(0, 10);

        Slice<PostJpaEntity> posts = recordQueryRepository.findPostsByRoom(
                testRoom.getRoomId(), "group", "latest", null, null, testUser.getUserId(), pageable
        );

        assertThat(posts.getContent()).allMatch(p -> {
            if (p instanceof RecordJpaEntity r) return r.isOverview();
            if (p instanceof VoteJpaEntity v) return v.isOverview();
            return false;
        });
    }
}