package konkuk.thip.comment.adapter.in.web;

import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.comment.adapter.out.jpa.CommentJpaEntity;
import konkuk.thip.comment.adapter.out.persistence.repository.CommentJpaRepository;
import konkuk.thip.comment.adapter.out.persistence.repository.CommentLikeJpaRepository;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.feed.adapter.out.jpa.FeedJpaEntity;
import konkuk.thip.feed.adapter.out.persistence.repository.FeedJpaRepository;
import konkuk.thip.roompost.adapter.out.jpa.RecordJpaEntity;
import konkuk.thip.roompost.adapter.out.persistence.repository.record.RecordJpaRepository;
import konkuk.thip.room.adapter.out.jpa.RoomJpaEntity;
import konkuk.thip.room.adapter.out.jpa.RoomParticipantRole;
import konkuk.thip.room.adapter.out.persistence.repository.RoomJpaRepository;
import konkuk.thip.room.adapter.out.persistence.repository.category.CategoryJpaRepository;
import konkuk.thip.room.adapter.out.persistence.repository.roomparticipant.RoomParticipantJpaRepository;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.user.adapter.out.persistence.repository.alias.AliasJpaRepository;
import konkuk.thip.roompost.adapter.out.jpa.VoteJpaEntity;
import konkuk.thip.roompost.adapter.out.persistence.repository.vote.VoteJpaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static konkuk.thip.common.entity.StatusType.INACTIVE;
import static konkuk.thip.post.domain.PostType.FEED;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("[통합] 댓글 삭제 api 통합 테스트")
class CommentDeleteApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired private AliasJpaRepository aliasJpaRepository;
    @Autowired private UserJpaRepository userJpaRepository;
    @Autowired private CategoryJpaRepository categoryJpaRepository;
    @Autowired private BookJpaRepository bookJpaRepository;
    @Autowired private FeedJpaRepository feedJpaRepository;
    @Autowired private VoteJpaRepository voteJpaRepository;
    @Autowired private RecordJpaRepository recordJpaRepository;
    @Autowired private CommentJpaRepository commentJpaRepository;
    @Autowired private RoomJpaRepository roomJpaRepository;
    @Autowired private RoomParticipantJpaRepository roomParticipantJpaRepository;
    @Autowired private CommentLikeJpaRepository commentLikeJpaRepository;

    private AliasJpaEntity alias;
    private UserJpaEntity user;
    private CategoryJpaEntity category;
    private FeedJpaEntity feed;
    private BookJpaEntity book;
    private RecordJpaEntity record;
    private VoteJpaEntity vote;
    private RoomJpaEntity room;

    @BeforeEach
    void setUp() {
        alias = aliasJpaRepository.save(TestEntityFactory.createLiteratureAlias());
        user = userJpaRepository.save(TestEntityFactory.createUser(alias));
        category = categoryJpaRepository.save(TestEntityFactory.createLiteratureCategory(alias));
        book = bookJpaRepository.save(TestEntityFactory.createBookWithISBN("9788954682152"));
        room = roomJpaRepository.save(TestEntityFactory.createRoom(book,category));
        feed = feedJpaRepository.save(TestEntityFactory.createFeed(user,book, true));
        record = recordJpaRepository.save(TestEntityFactory.createRecord(user,room));
        vote = voteJpaRepository.save(TestEntityFactory.createVote(user,room));
        roomParticipantJpaRepository.save(TestEntityFactory.createRoomParticipant(room, user, RoomParticipantRole.HOST, 0.0));
    }

    @AfterEach
    void tearDown() {
        recordJpaRepository.deleteAllInBatch();
        voteJpaRepository.deleteAllInBatch();
        commentLikeJpaRepository.deleteAll();
        commentJpaRepository.deleteAllInBatch();
        feedJpaRepository.deleteAllInBatch();
        roomParticipantJpaRepository.deleteAllInBatch();
        roomJpaRepository.deleteAllInBatch();
        bookJpaRepository.deleteAll();
        userJpaRepository.deleteAllInBatch();
        categoryJpaRepository.deleteAll();
        aliasJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("루트댓글을 삭제하면 [soft delete 처리]된다")
    void deleteRootComment_success() throws Exception {

        // given
        CommentJpaEntity comment = commentJpaRepository.save(TestEntityFactory.createComment(feed, user, FEED));
        feed.updateCommentCount(1);
        feedJpaRepository.save(feed);
        Long commentId = comment.getCommentId();

        // when
        mockMvc.perform(delete("/comments/{commentId}", commentId)
                        .requestAttr("userId", user.getUserId()))
                .andExpect(status().isOk());

        // then
        assertThat(commentJpaRepository.findByCommentIdAndStatus(commentId,INACTIVE)).isPresent();
    }

    @Test
    @DisplayName("대댓글을 삭제하면 [soft delete 처리]된다")
    void deleteReplyComment_success() throws Exception {

        // given: 부모 댓글/대댓글 생성
        CommentJpaEntity parent = commentJpaRepository.save(TestEntityFactory.createComment(feed, user, FEED));
        CommentJpaEntity reply = commentJpaRepository.save(TestEntityFactory.createReplyComment(feed, user, FEED, parent));
        feed.updateCommentCount(2);
        feedJpaRepository.save(feed);
        Long replyId = reply.getCommentId();

        // when
        mockMvc.perform(delete("/comments/{commentId}", replyId)
                        .requestAttr("userId", user.getUserId()))
                .andExpect(status().isOk());

        // then
        assertThat(commentJpaRepository.findByCommentIdAndStatus(replyId,INACTIVE)).isPresent();
    }

    @Test
    @DisplayName("댓글 삭제 시에 댓글을 성공적으로 삭제하면 댓글의 상태가 soft delete 처리되고" +
            "게시물의 댓글 수가 1감소하고, 댓글에 달린 좋아요가 전체 삭제 된다.")
    void deleteComment_success() throws Exception {

        // given
        CommentJpaEntity comment = commentJpaRepository.save(TestEntityFactory.createComment(feed, user, FEED));
        commentLikeJpaRepository.save(TestEntityFactory.createCommentLike(comment, user));
        feed.updateCommentCount(1);
        feedJpaRepository.save(feed);
        Long commentId = comment.getCommentId();
        int beforeCount = feed.getCommentCount();

        // when
        mockMvc.perform(delete("/comments/{commentId}", commentId)
                        .requestAttr("userId", user.getUserId()))
                .andExpect(status().isOk());

        // then
        assertThat(commentJpaRepository.findByCommentIdAndStatus(commentId,INACTIVE)).isPresent();

        // Feed 댓글수 감소 확인
        FeedJpaEntity updatedFeed = feedJpaRepository.findById(feed.getPostId()).get();
        assertThat(updatedFeed.getCommentCount()).isEqualTo(beforeCount - 1);

        // 댓글 좋아요가 모두 삭제됐는지 확인
        boolean like = commentLikeJpaRepository.existsByUserIdAndCommentId(user.getUserId(), commentId);
        assertThat(like).isFalse();
    }

}
