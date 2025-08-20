package konkuk.thip.roompost.adapter.in.web;

import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.comment.adapter.out.jpa.CommentJpaEntity;
import konkuk.thip.comment.adapter.out.persistence.repository.CommentJpaRepository;
import konkuk.thip.comment.adapter.out.persistence.repository.CommentLikeJpaRepository;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.post.adapter.out.persistence.PostLikeJpaRepository;
import konkuk.thip.room.domain.value.Category;
import konkuk.thip.roompost.adapter.out.jpa.RecordJpaEntity;
import konkuk.thip.roompost.adapter.out.persistence.repository.record.RecordJpaRepository;
import konkuk.thip.room.adapter.out.jpa.RoomJpaEntity;
import konkuk.thip.room.adapter.out.jpa.RoomParticipantRole;
import konkuk.thip.room.adapter.out.persistence.repository.RoomJpaRepository;
import konkuk.thip.room.adapter.out.persistence.repository.roomparticipant.RoomParticipantJpaRepository;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.user.domain.value.Alias;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static konkuk.thip.common.entity.StatusType.INACTIVE;
import static konkuk.thip.post.domain.PostType.RECORD;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("[통합] 기록 삭제 api 통합 테스트")
class RecordDeleteApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired private UserJpaRepository userJpaRepository;
    @Autowired private BookJpaRepository bookJpaRepository;
    @Autowired private CommentJpaRepository commentJpaRepository;
    @Autowired private CommentLikeJpaRepository commentLikeJpaRepository;
    @Autowired private PostLikeJpaRepository postLikeJpaRepository;
    @Autowired private RecordJpaRepository recordJpaRepository;
    @Autowired private RoomJpaRepository roomJpaRepository;
    @Autowired private RoomParticipantJpaRepository roomParticipantJpaRepository;

    private Alias alias;
    private UserJpaEntity user;
    private Category category;
    private BookJpaEntity book;
    private CommentJpaEntity comment;
    private RecordJpaEntity record;
    private RoomJpaEntity room;

    @BeforeEach
    void setUp() {
        alias = TestEntityFactory.createLiteratureAlias();
        user = userJpaRepository.save(TestEntityFactory.createUser(alias));
        category = TestEntityFactory.createLiteratureCategory();
        book = bookJpaRepository.save(TestEntityFactory.createBookWithISBN("9788954682152"));
        room = roomJpaRepository.save(TestEntityFactory.createRoom(book,category));
        record = recordJpaRepository.save(TestEntityFactory.createRecord(user,room));
        roomParticipantJpaRepository.save(TestEntityFactory.createRoomParticipant(room, user, RoomParticipantRole.HOST, 0.0));
        postLikeJpaRepository.save(TestEntityFactory.createPostLike(user,record));
        comment = commentJpaRepository.save(TestEntityFactory.createComment(record, user, RECORD));
        commentLikeJpaRepository.save(TestEntityFactory.createCommentLike(comment,user));
        record.updateLikeCount(1);
        record.updateCommentCount(1);
        recordJpaRepository.save(record);
        comment.updateLikeCount(1);
        commentJpaRepository.save(comment);
    }

    @Test
    @DisplayName("기록을 삭제하면 [soft delete]되고, 연관된 댓글, 댓글 좋아요도 모두 삭제된다")
    void deleteRecord_success() throws Exception {

        // when
        mockMvc.perform(delete("/rooms/{roomId}/record/{recordId}", room.getRoomId(), record.getPostId())
                        .requestAttr("userId", user.getUserId()))
                .andExpect(status().isOk());


        // then: 1) 기록 soft delete (status=INACTIVE)
        assertThat(recordJpaRepository.findByPostIdAndStatus(record.getPostId(), INACTIVE)).isPresent();

        // 2) 댓글 삭제 soft delete
        assertThat(commentJpaRepository.findByCommentIdAndStatus(comment.getCommentId(),INACTIVE)).isPresent();

        // 3) 댓글 좋아요 삭제
        assertThat(commentLikeJpaRepository.count()).isEqualTo(0);

        // 4) 게시글 좋아요(PostLike) 삭제
        assertThat(postLikeJpaRepository.count()).isEqualTo(0);

    }
}
