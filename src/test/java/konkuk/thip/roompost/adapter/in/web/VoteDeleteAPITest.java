package konkuk.thip.roompost.adapter.in.web;

import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.comment.adapter.out.jpa.CommentJpaEntity;
import konkuk.thip.comment.adapter.out.persistence.repository.CommentJpaRepository;
import konkuk.thip.comment.adapter.out.persistence.repository.CommentLikeJpaRepository;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.post.adapter.out.persistence.PostLikeJpaRepository;
import konkuk.thip.room.adapter.out.jpa.CategoryJpaEntity;
import konkuk.thip.room.adapter.out.jpa.RoomJpaEntity;
import konkuk.thip.room.adapter.out.jpa.RoomParticipantRole;
import konkuk.thip.room.adapter.out.persistence.repository.RoomJpaRepository;
import konkuk.thip.room.adapter.out.persistence.repository.category.CategoryJpaRepository;
import konkuk.thip.room.adapter.out.persistence.repository.roomparticipant.RoomParticipantJpaRepository;
import konkuk.thip.user.adapter.out.jpa.AliasJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.user.adapter.out.persistence.repository.alias.AliasJpaRepository;
import konkuk.thip.roompost.adapter.out.jpa.VoteItemJpaEntity;
import konkuk.thip.roompost.adapter.out.jpa.VoteJpaEntity;
import konkuk.thip.roompost.adapter.out.persistence.repository.vote.VoteItemJpaRepository;
import konkuk.thip.roompost.adapter.out.persistence.repository.vote.VoteJpaRepository;
import konkuk.thip.roompost.adapter.out.persistence.repository.vote.VoteParticipantJpaRepository;
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
import static konkuk.thip.post.domain.PostType.VOTE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("[통합] 투표 삭제 api 통합 테스트")
class VoteDeleteAPITest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired private AliasJpaRepository aliasJpaRepository;
    @Autowired private UserJpaRepository userJpaRepository;
    @Autowired private CategoryJpaRepository categoryJpaRepository;
    @Autowired private BookJpaRepository bookJpaRepository;
    @Autowired private CommentJpaRepository commentJpaRepository;
    @Autowired private CommentLikeJpaRepository commentLikeJpaRepository;
    @Autowired private PostLikeJpaRepository postLikeJpaRepository;
    @Autowired private VoteJpaRepository voteJpaRepository;
    @Autowired private VoteItemJpaRepository voteItemJpaRepository;
    @Autowired private VoteParticipantJpaRepository voteParticipantJpaRepository;
    @Autowired private RoomJpaRepository roomJpaRepository;
    @Autowired private RoomParticipantJpaRepository roomParticipantJpaRepository;

    private AliasJpaEntity alias;
    private UserJpaEntity user1;
    private UserJpaEntity user2;
    private CategoryJpaEntity category;
    private BookJpaEntity book;
    private CommentJpaEntity comment;
    private RoomJpaEntity room;
    private VoteJpaEntity vote;
    private VoteItemJpaEntity voteItem1;
    private VoteItemJpaEntity voteItem2;

    @BeforeEach
    void setUp() {
        alias = aliasJpaRepository.save(TestEntityFactory.createLiteratureAlias());
        user1 = userJpaRepository.save(TestEntityFactory.createUser(alias));
        user2 = userJpaRepository.save(TestEntityFactory.createUser(alias));
        category = categoryJpaRepository.save(TestEntityFactory.createLiteratureCategory(alias));
        book = bookJpaRepository.save(TestEntityFactory.createBookWithISBN("9788954682152"));
        room = roomJpaRepository.save(TestEntityFactory.createRoom(book,category));
        // 유저 1이 호스트, 유저 2가 멤버로 방 참여
        roomParticipantJpaRepository.save(TestEntityFactory.createRoomParticipant(room, user1, RoomParticipantRole.HOST, 0.0));
        roomParticipantJpaRepository.save(TestEntityFactory.createRoomParticipant(room, user2, RoomParticipantRole.MEMBER, 0.0));
        // 투표 생성자 유저 1
        vote = voteJpaRepository.save(TestEntityFactory.createVote(user1,room));
        // 투표 항목 2개 생성
        voteItem1 = voteItemJpaRepository.save(TestEntityFactory.createVoteItem("예시투표1",vote));
        voteItem2 = voteItemJpaRepository.save(TestEntityFactory.createVoteItem("예시투표2",vote));
        // 투표항목 1에 유저 1 투표
        voteParticipantJpaRepository.save(TestEntityFactory.createVoteParticipant(user1,voteItem1));
        // 투표항목 2에 유저 2 투표
        voteParticipantJpaRepository.save(TestEntityFactory.createVoteParticipant(user2,voteItem2));

        postLikeJpaRepository.save(TestEntityFactory.createPostLike(user1,vote));
        postLikeJpaRepository.save(TestEntityFactory.createPostLike(user2,vote));

        comment = commentJpaRepository.save(TestEntityFactory.createComment(vote, user1, VOTE));
        commentLikeJpaRepository.save(TestEntityFactory.createCommentLike(comment,user1));

        vote.updateLikeCount(2);
        vote.updateCommentCount(1);
        voteJpaRepository.save(vote);
        comment.updateLikeCount(1);
        commentJpaRepository.save(comment);
    }

    @Test
    @DisplayName("투표를 삭제하면 [soft delete]되고, 연관된 댓글, 댓글 좋아요, 투표 항목, 투표 참여 관계도 모두 삭제된다")
    void deleteVote_success() throws Exception {

        // when
        mockMvc.perform(delete("/rooms/{roomId}/vote/{voteId}", room.getRoomId(), vote.getPostId())
                        .requestAttr("userId", user1.getUserId()))
                .andExpect(status().isOk());


        // then: 1) 투표 soft delete (status=INACTIVE)
        assertThat(voteJpaRepository.findByPostIdAndStatus(vote.getPostId(), INACTIVE)).isPresent();

        // 2) 댓글 삭제 soft delete
        assertThat(commentJpaRepository.findByCommentIdAndStatus(comment.getCommentId(),INACTIVE)).isPresent();

        // 3) 댓글 좋아요 물리 삭제
        assertThat(commentLikeJpaRepository.count()).isEqualTo(0);

        // 4) 투표 항목 물리 삭제
        assertThat(voteItemJpaRepository.count()).isEqualTo(0);

        // 5) 투표 참여관계 물리 삭제
        assertThat(voteParticipantJpaRepository.count()).isEqualTo(0);

        // 6) 게시글 좋아요(PostLike) 삭제
        assertThat(postLikeJpaRepository.count()).isEqualTo(0);
    }
}
