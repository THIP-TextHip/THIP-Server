package konkuk.thip.comment.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.comment.adapter.in.web.request.CommentIsLikeRequest;
import konkuk.thip.comment.adapter.out.jpa.CommentJpaEntity;
import konkuk.thip.comment.adapter.out.jpa.CommentLikeJpaEntity;
import konkuk.thip.comment.adapter.out.persistence.repository.CommentJpaRepository;
import konkuk.thip.comment.adapter.out.persistence.repository.CommentLikeJpaRepository;
import konkuk.thip.common.exception.code.ErrorCode;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.feed.adapter.out.jpa.FeedJpaEntity;
import konkuk.thip.feed.adapter.out.persistence.repository.FeedJpaRepository;
import konkuk.thip.room.adapter.out.jpa.CategoryJpaEntity;
import konkuk.thip.room.adapter.out.jpa.RoomJpaEntity;
import konkuk.thip.room.adapter.out.persistence.repository.RoomJpaRepository;
import konkuk.thip.room.adapter.out.persistence.repository.category.CategoryJpaRepository;
import konkuk.thip.user.adapter.out.jpa.AliasJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.user.adapter.out.persistence.repository.alias.AliasJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static konkuk.thip.post.domain.PostType.FEED;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("[통합] 댓글 좋아요 상태 변경 api 통합 테스트")
class CommentChangeLikeStatusApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;
    @Autowired private AliasJpaRepository aliasJpaRepository;
    @Autowired private UserJpaRepository userJpaRepository;
    @Autowired private CategoryJpaRepository categoryJpaRepository;
    @Autowired private BookJpaRepository bookJpaRepository;
    @Autowired private FeedJpaRepository feedJpaRepository;
    @Autowired private CommentJpaRepository commentJpaRepository;
    @Autowired private CommentLikeJpaRepository commentLikeJpaRepository;
    @Autowired private RoomJpaRepository roomJpaRepository;


    private AliasJpaEntity alias;
    private UserJpaEntity user;
    private CategoryJpaEntity category;
    private FeedJpaEntity feed;
    private BookJpaEntity book;
    private RoomJpaEntity room;
    private CommentJpaEntity comment;

    @BeforeEach
    void setUp() {
        alias = aliasJpaRepository.save(TestEntityFactory.createLiteratureAlias());
        user = userJpaRepository.save(TestEntityFactory.createUser(alias));
        category = categoryJpaRepository.save(TestEntityFactory.createLiteratureCategory(alias));
        book = bookJpaRepository.save(TestEntityFactory.createBookWithISBN("9788954682152"));
        room = roomJpaRepository.save(TestEntityFactory.createRoom(book,category));
        feed = feedJpaRepository.save(TestEntityFactory.createFeed(user,book, true));
        comment = commentJpaRepository.save(TestEntityFactory.createComment(feed,user, FEED));
    }

    private static final String COMMENT_LIKE_API_PATH = "/comments/{commentId}/likes";


    @Test
    @DisplayName("댓글을 처음 좋아요하면 [좋아요 성공]")
    void likeComment_success() throws Exception {

        // given
        CommentIsLikeRequest request = new CommentIsLikeRequest(true);

        // when
        mockMvc.perform(post(COMMENT_LIKE_API_PATH, comment.getCommentId())
                        .requestAttr("userId", user.getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.commentId").value(comment.getCommentId()))
                .andExpect(jsonPath("$.data.isLiked").value(true));

        // then
        // 1. 좋아요 저장 여부 확인
        List<CommentLikeJpaEntity> likedComments = commentLikeJpaRepository.findAllByUserId(user.getUserId());
        boolean exists = likedComments.stream()
                        .anyMatch(entity -> entity.getCommentJpaEntity().getCommentId().equals(comment.getCommentId()));
        assertThat(exists).isTrue();

        // 2. 댓글 좋아요 수가 1 증가했는지 확인
        CommentJpaEntity updatedComment = commentJpaRepository.findById(comment.getCommentId()).orElse(null);
        assertThat(updatedComment.getLikeCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("이미 좋아요한 댓글을 다시 좋아요하려면 [400 에러 발생]")
    void likeComment_alreadyLiked_fail() throws Exception {

        // given
        commentLikeJpaRepository.save(TestEntityFactory.createCommentLike(comment,user));
        CommentIsLikeRequest request = new CommentIsLikeRequest(true);

        // when & then
        mockMvc.perform(post(COMMENT_LIKE_API_PATH, comment.getCommentId())
                        .requestAttr("userId", user.getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.COMMENT_ALREADY_LIKED.getCode()));
    }

    @Test
    @DisplayName("댓글을 좋아요한 이후 좋아요 취소 요청하면 [좋아요 취소 성공]")
    void unlikeComment_success() throws Exception {

        // given
        commentLikeJpaRepository.save(TestEntityFactory.createCommentLike(comment,user));
        comment.updateLikeCount(1);
        CommentIsLikeRequest request = new CommentIsLikeRequest(false);

        mockMvc.perform(post(COMMENT_LIKE_API_PATH, comment.getCommentId())
                        .requestAttr("userId", user.getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.commentId").value(comment.getCommentId()))
                .andExpect(jsonPath("$.data.isLiked").value(false));

        // then
        // 1. 좋아요 삭제 여부 확인
        List<CommentLikeJpaEntity> likedComments = commentLikeJpaRepository.findAllByUserId(user.getUserId());
        boolean exists = likedComments.stream()
                .anyMatch(entity -> entity.getCommentJpaEntity().getCommentId().equals(comment.getCommentId()));
        assertThat(exists).isFalse();

        // 2. 댓글 좋아요 수가 0으로 감소했는지 확인
        CommentJpaEntity updatedComment = commentJpaRepository.findById(comment.getCommentId()).orElse(null);
        assertThat(updatedComment.getLikeCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("좋아요하지 않은 댓글을 좋아요 취소하려고 하면 [400 에러 발생]")
    void unlikeComment_notLiked_fail() throws Exception {
        // given
        CommentIsLikeRequest request = new CommentIsLikeRequest(false);

        // when & then
        mockMvc.perform(post(COMMENT_LIKE_API_PATH, comment.getCommentId())
                        .requestAttr("userId", user.getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.COMMENT_NOT_LIKED_CANNOT_CANCEL.getCode()));
    }


    @Test
    @DisplayName("존재하지 않는 댓글 좋아요 요청 시 [404 에러 발생]")
    void likeComment_commentNotFound_fail() throws Exception {

        // given
        Long invalidCommentId = 999999L;
        CommentIsLikeRequest request = new CommentIsLikeRequest(true);

        // when
        mockMvc.perform(post(COMMENT_LIKE_API_PATH, invalidCommentId)
                        .requestAttr("userId", user.getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.COMMENT_NOT_FOUND.getCode()));
    }


}
