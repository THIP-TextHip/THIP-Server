package konkuk.thip.user.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.book.adapter.out.persistence.repository.SavedBookJpaRepository;
import konkuk.thip.comment.adapter.out.jpa.CommentJpaEntity;
import konkuk.thip.comment.adapter.out.jpa.CommentLikeJpaEntity;
import konkuk.thip.comment.adapter.out.persistence.repository.CommentJpaRepository;
import konkuk.thip.comment.adapter.out.persistence.repository.CommentLikeJpaRepository;
import konkuk.thip.common.security.util.JwtUtil;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.feed.adapter.out.jpa.FeedJpaEntity;
import konkuk.thip.feed.adapter.out.persistence.repository.FeedJpaRepository;
import konkuk.thip.feed.adapter.out.persistence.repository.SavedFeedJpaRepository;
import konkuk.thip.post.adapter.out.jpa.PostLikeJpaEntity;
import konkuk.thip.post.adapter.out.persistence.PostLikeJpaRepository;
import konkuk.thip.recentSearch.adapter.out.persistence.repository.RecentSearchJpaRepository;
import konkuk.thip.room.adapter.out.jpa.RoomJpaEntity;
import konkuk.thip.room.adapter.out.jpa.RoomParticipantJpaEntity;
import konkuk.thip.room.adapter.out.jpa.RoomStatus;
import konkuk.thip.room.adapter.out.persistence.repository.RoomJpaRepository;
import konkuk.thip.room.adapter.out.persistence.repository.roomparticipant.RoomParticipantJpaRepository;
import konkuk.thip.room.domain.value.Category;
import konkuk.thip.roompost.adapter.out.jpa.*;
import konkuk.thip.roompost.adapter.out.persistence.repository.attendancecheck.AttendanceCheckJpaRepository;
import konkuk.thip.roompost.adapter.out.persistence.repository.record.RecordJpaRepository;
import konkuk.thip.roompost.adapter.out.persistence.repository.vote.VoteItemJpaRepository;
import konkuk.thip.roompost.adapter.out.persistence.repository.vote.VoteJpaRepository;
import konkuk.thip.roompost.adapter.out.persistence.repository.vote.VoteParticipantJpaRepository;
import konkuk.thip.user.adapter.out.jpa.FollowingJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.user.adapter.out.persistence.repository.following.FollowingJpaRepository;
import konkuk.thip.user.application.port.UserTokenBlacklistQueryPort;
import konkuk.thip.user.domain.value.Alias;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static konkuk.thip.common.entity.StatusType.INACTIVE;
import static konkuk.thip.common.exception.code.ErrorCode.USER_CANNOT_DELETE_ROOM_HOST;
import static konkuk.thip.post.domain.PostType.*;
import static konkuk.thip.room.adapter.out.jpa.RoomParticipantRole.HOST;
import static konkuk.thip.room.adapter.out.jpa.RoomParticipantRole.MEMBER;
import static konkuk.thip.room.adapter.out.jpa.RoomStatus.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@DisplayName("[통합] 회원탈퇴 api 테스트")
public class UserDeleteApiTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserJpaRepository userJpaRepository;
    @Autowired private BookJpaRepository bookJpaRepository;
    @Autowired private FeedJpaRepository feedJpaRepository;
    @Autowired private CommentJpaRepository commentJpaRepository;
    @Autowired private CommentLikeJpaRepository commentLikeJpaRepository;
    @Autowired private PostLikeJpaRepository postLikeJpaRepository;
    @Autowired private SavedFeedJpaRepository savedFeedJpaRepository;
    @Autowired private RoomJpaRepository roomJpaRepository;
    @Autowired private RoomParticipantJpaRepository roomParticipantJpaRepository;
    @Autowired private VoteJpaRepository voteJpaRepository;
    @Autowired private VoteItemJpaRepository voteItemJpaRepository;
    @Autowired private FollowingJpaRepository followingJpaRepository;
    @Autowired private RecentSearchJpaRepository recentSearchJpaRepository;
    @Autowired private SavedBookJpaRepository savedBookJpaRepository;
    @Autowired private AttendanceCheckJpaRepository attendanceCheckJpaRepository;
    @Autowired private VoteParticipantJpaRepository voteParticipantJpaRepository;
    @Autowired private RecordJpaRepository recordJpaRepository;
    @Autowired private UserTokenBlacklistQueryPort userTokenBlacklistQueryPort;

    @Autowired private JwtUtil jwtUtil;
    @Autowired private EntityManager entityManager;

    @AfterEach
    void tearDown() {
        followingJpaRepository.deleteAllInBatch();
        recentSearchJpaRepository.deleteAllInBatch();
        savedFeedJpaRepository.deleteAllInBatch();
        savedBookJpaRepository.deleteAllInBatch();
        attendanceCheckJpaRepository.deleteAllInBatch();
        voteParticipantJpaRepository.deleteAllInBatch();
        commentLikeJpaRepository.deleteAllInBatch();
        commentJpaRepository.deleteAllInBatch();
        postLikeJpaRepository.deleteAllInBatch();
        feedJpaRepository.deleteAllInBatch();
        recordJpaRepository.deleteAllInBatch();
        voteItemJpaRepository.deleteAllInBatch();
        voteJpaRepository.deleteAllInBatch();
        roomParticipantJpaRepository.deleteAllInBatch();
        roomJpaRepository.deleteAll();
        bookJpaRepository.deleteAll();
        userJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("회원탈퇴 성공시 모든 연관 엔티티가 각 엔티티 삭제 전략에 맞게 삭제되고 탈퇴한 회원의 토큰이 블랙리스트에 등록된다.")
    void deleteUser_success() throws Exception {

        // given
        // 유저 설정 1:테스트하고자하는 유저 2:방의 호스트 유저 3:방의 멤버유저
        UserJpaEntity testUser1 = userJpaRepository.save(TestEntityFactory.createUser(Alias.ARTIST));
        UserJpaEntity otherHostUser2 = userJpaRepository.save(TestEntityFactory.createUser(Alias.ARTIST));
        UserJpaEntity otherMemberUser3 = userJpaRepository.save(TestEntityFactory.createUser(Alias.ARTIST));

        // 팔로잉 관계 설정
        FollowingJpaEntity u1_u2_f1 = followingJpaRepository.save(TestEntityFactory.createFollowing(testUser1, otherHostUser2)); // 유저 1이 유저 2 팔로우
        FollowingJpaEntity u3_u1_f2 = followingJpaRepository.save(TestEntityFactory.createFollowing(otherMemberUser3, testUser1)); // 유저 3이 유저 1팔로우
        followingJpaRepository.save(TestEntityFactory.createFollowing(otherMemberUser3, otherHostUser2)); // 유저 3이 유저 2팔로우
        otherHostUser2.setFollowerCount(2); userJpaRepository.save(otherHostUser2); //유저 2 팔로워수 2
        testUser1.setFollowerCount(1); userJpaRepository.save(testUser1); //유저 1 팔로워수 1

        // 최근검색어 저장
        recentSearchJpaRepository.save(TestEntityFactory.createRecentSearch(testUser1));

        BookJpaEntity book = bookJpaRepository.save(TestEntityFactory.createBook());
        Category category = TestEntityFactory.createLiteratureCategory();
        // 방 참여 관계 설정: 모든 유저가 방에 참여해있음
        RoomJpaEntity roomExpired1 = createRoom(book,category, EXPIRED);
        RoomJpaEntity roomInProgress2 = createRoom(book,category, IN_PROGRESS);
        RoomJpaEntity roomRecruiting3 = createRoom(book,category, RECRUITING);

        // 방1-> 만료된 방 : 유저1이 HOST
        RoomParticipantJpaEntity r1_rp1 = roomParticipantJpaRepository.save(TestEntityFactory.createRoomParticipant(roomExpired1,testUser1,HOST,50.0));
        RoomParticipantJpaEntity r1_rp2 = roomParticipantJpaRepository.save(TestEntityFactory.createRoomParticipant(roomExpired1,otherHostUser2, MEMBER,30.0));
        RoomParticipantJpaEntity r1_rp3 = roomParticipantJpaRepository.save(TestEntityFactory.createRoomParticipant(roomExpired1,otherMemberUser3,MEMBER,60.0));
        updateRoomPercentage(r1_rp1,r1_rp2,r1_rp3,roomExpired1);
        // 방2-> 진행중인 방 : 유저2가 HOST
        RoomParticipantJpaEntity r2_rp1 = roomParticipantJpaRepository.save(TestEntityFactory.createRoomParticipant(roomInProgress2,testUser1,MEMBER,50.0));
        RoomParticipantJpaEntity r2_rp2 = roomParticipantJpaRepository.save(TestEntityFactory.createRoomParticipant(roomInProgress2,otherHostUser2,HOST,30.0));
        RoomParticipantJpaEntity r2_rp3 = roomParticipantJpaRepository.save(TestEntityFactory.createRoomParticipant(roomInProgress2,otherMemberUser3,MEMBER,60.0));
        updateRoomPercentage(r2_rp1,r2_rp2,r2_rp3,roomInProgress2);
        // 방3 -> 모집중인 방 : 유저2가 HOST
        RoomParticipantJpaEntity r3_rp1 = roomParticipantJpaRepository.save(TestEntityFactory.createRoomParticipant(roomRecruiting3,testUser1,MEMBER,50.0));
        RoomParticipantJpaEntity r3_rp2 = roomParticipantJpaRepository.save(TestEntityFactory.createRoomParticipant(roomRecruiting3,otherHostUser2,HOST,30.0));
        RoomParticipantJpaEntity r3_rp3 = roomParticipantJpaRepository.save(TestEntityFactory.createRoomParticipant(roomRecruiting3,otherMemberUser3,MEMBER,60.0));
        updateRoomPercentage(r3_rp1,r3_rp2,r3_rp3,roomRecruiting3);

        // 피드/책 저장관계 설정
        FeedJpaEntity u2_f1 = feedJpaRepository.save(TestEntityFactory.createFeed(otherHostUser2,book,true)); //유저 2가 피드1 작성
        savedFeedJpaRepository.save(TestEntityFactory.createSavedFeed(testUser1,u2_f1)); //유저 1이 유저2가 작성한 피드1를 저장

        BookJpaEntity sb = bookJpaRepository.save(TestEntityFactory.createBook());
        savedBookJpaRepository.save(TestEntityFactory.createSavedBook(testUser1,sb));

        // 오늘의 한마디 관계 설정
        AttendanceCheckJpaEntity a = attendanceCheckJpaRepository.save(TestEntityFactory.createAttendanceCheck
                ("유저1이 방2에 남긴 오늘의한마디1",roomInProgress2,testUser1));

        //유저 2가 방2에 투표1 생성
        VoteJpaEntity u2_v1 = voteJpaRepository.save(
                VoteJpaEntity.builder().content("유저2가 방2에 생성한 투표1").userJpaEntity(otherHostUser2).page(33).isOverview(true)
                        .commentCount(2).likeCount(1).roomJpaEntity(roomInProgress2).build());
        VoteItemJpaEntity v1_vi1 = voteItemJpaRepository.save(
                VoteItemJpaEntity.builder().itemName("유저2가 방2에 생성한 투표1의 항목1").count(1).voteJpaEntity(u2_v1).build());
        VoteItemJpaEntity v1_vi2 = voteItemJpaRepository.save(
                VoteItemJpaEntity.builder().itemName("유저2가 방2에 생성한 투표1의 항목2").count(0).voteJpaEntity(u2_v1).build());
        VoteParticipantJpaEntity u1_vp1 = voteParticipantJpaRepository.save(TestEntityFactory.createVoteParticipant(testUser1, v1_vi1)); //유저1이 투표1의 투표항목1에 투표
        CommentJpaEntity u2_v1_c1 = commentJpaRepository.save(
                CommentJpaEntity.builder().content("유저2가 투표1에 남긴 댓글1").postJpaEntity(u2_v1).userJpaEntity(otherHostUser2)
                        .likeCount(1).reportCount(0).postType(VOTE).build()); //유저2가 투표1에 댓글
        // 유저1의 댓글 좋아요 관계 설정
        CommentLikeJpaEntity u1_c1_cl1 = commentLikeJpaRepository.save(TestEntityFactory.createCommentLike(u2_v1_c1, testUser1)); //유저1이 댓글1에 댓글좋아요

        //유저2가 방2에 기록1 생성
        RecordJpaEntity u2_r1 = recordJpaRepository.save(
                RecordJpaEntity.builder().content("유저2가 방2에 생성한 기록1").userJpaEntity(otherHostUser2).page(22).isOverview(false)
                        .commentCount(1).likeCount(1).roomJpaEntity(roomInProgress2).build());

        // 유저1의 게시글 좋아요 관계 설정
        PostLikeJpaEntity u1_v1_pl1 = postLikeJpaRepository.save(TestEntityFactory.createPostLike(testUser1,u2_v1)); //유저1이 투표1에 좋아요
        PostLikeJpaEntity u1_f1_pl2 = postLikeJpaRepository.save(TestEntityFactory.createPostLike(testUser1,u2_f1)); //유저1이 피드1에 좋아요
        PostLikeJpaEntity u1_r1_pl3 = postLikeJpaRepository.save(TestEntityFactory.createPostLike(testUser1,u2_r1)); //유저1이 기록1에 좋아요

        // 유저1의 댓글 저장
        CommentJpaEntity u1_v1_c2 = commentJpaRepository.save(
                CommentJpaEntity.builder().content("유저1이 투표1에 남긴 댓글2").postJpaEntity(u2_v1).userJpaEntity(testUser1)
                        .likeCount(1).reportCount(0).postType(VOTE).build()); //유저1이 투표1에 댓글
        CommentJpaEntity u1_f1_c3 = commentJpaRepository.save(
                CommentJpaEntity.builder().content("유저1이 피드1에 남긴 댓글3").postJpaEntity(u2_f1).userJpaEntity(testUser1)
                        .likeCount(1).reportCount(0).postType(FEED).build()); //유저1이 피드1에 댓글
        CommentJpaEntity u1_r1_c4 = commentJpaRepository.save(
                CommentJpaEntity.builder().content("유저1이 기록1에 남긴 댓글4").postJpaEntity(u2_r1).userJpaEntity(testUser1)
                        .likeCount(1).reportCount(0).postType(RECORD).build()); //유저1이 기록1에 댓글

        u2_f1.setLikeCount(1); //피드 1 좋아요/댓글 씽크 맞추기
        u2_f1.setCommentCount(1);
        feedJpaRepository.save(u2_f1);

        // 유저1이 남긴 댓글에 대해 댓글 좋아요 관계 설정 : 유저2가 유저1이 남긴 댓글에 대해 좋아요
        CommentLikeJpaEntity u2_c2_cl = commentLikeJpaRepository.save(
                TestEntityFactory.createCommentLike(u1_v1_c2, otherHostUser2));
        CommentLikeJpaEntity u2_c3_cl = commentLikeJpaRepository.save(
                TestEntityFactory.createCommentLike(u1_f1_c3, otherHostUser2));
        CommentLikeJpaEntity u2_c4_cl = commentLikeJpaRepository.save(
                TestEntityFactory.createCommentLike(u1_r1_c4, otherHostUser2));

        // 유저1이 작성한 게시물 관계 설정
        // 유저 1이 피드2 작성
        FeedJpaEntity u1_f2 = feedJpaRepository.save(TestEntityFactory.createFeed(testUser1,book,true));
        CommentJpaEntity u2_f3_c4 = commentJpaRepository.save(
                CommentJpaEntity.builder().content("유저2이 피드2에 남긴 댓글4").postJpaEntity(u1_f2).userJpaEntity(otherHostUser2)
                        .likeCount(1).reportCount(0).postType(FEED).build()); //유저2이 피드2에 댓글
        //유저3이 댓글4에 댓글좋아요
        CommentLikeJpaEntity u3_c4_cl2 = commentLikeJpaRepository.save(TestEntityFactory.createCommentLike(u2_f3_c4, otherMemberUser3));
        PostLikeJpaEntity u2_f2_pl4 = postLikeJpaRepository.save(TestEntityFactory.createPostLike(otherHostUser2, u1_f2)); //유저2가 피드2에 좋아요
        savedFeedJpaRepository.save(TestEntityFactory.createSavedFeed(otherHostUser2,u1_f2)); //유저2가 피드2를 저장
        u1_f2.setLikeCount(1); //피드 2 좋아요/댓글 씽크 맞추기
        u1_f2.setCommentCount(1);
        feedJpaRepository.save(u1_f2);

        // 유저 1이 투표2 작성
        VoteJpaEntity u1_v2 = voteJpaRepository.save(
                VoteJpaEntity.builder().content("유저1가 방2에 생성한 투표2").userJpaEntity(testUser1).page(33).isOverview(true)
                        .commentCount(1).likeCount(1).roomJpaEntity(roomInProgress2).build());
        VoteItemJpaEntity v2_vi1 = voteItemJpaRepository.save(
                VoteItemJpaEntity.builder().itemName("유저1이 방2에 생성한 투표2의 항목1").count(1).voteJpaEntity(u1_v2).build());
        VoteItemJpaEntity v2_vi2 = voteItemJpaRepository.save(
                VoteItemJpaEntity.builder().itemName("유저1이 방2에 생성한 투표2의 항목2").count(1).voteJpaEntity(u1_v2).build());
        VoteParticipantJpaEntity u2_vp2 = voteParticipantJpaRepository.save(TestEntityFactory.createVoteParticipant(otherHostUser2, v2_vi1)); //유저2이 투표2의 투표항목1에 투표
        VoteParticipantJpaEntity u3_vp3 = voteParticipantJpaRepository.save(TestEntityFactory.createVoteParticipant(otherMemberUser3, v2_vi2)); //유저3이 투표2의 투표항목2에 투표
        CommentJpaEntity u3_v2_c5 = commentJpaRepository.save(
                CommentJpaEntity.builder().content("유저3이 투표2에 남긴 댓글5").postJpaEntity(u1_v2).userJpaEntity(otherMemberUser3)
                        .likeCount(1).reportCount(0).postType(VOTE).build()); //유저3이 투표2에 댓글
        //유저2이 댓글5에 댓글좋아요
        CommentLikeJpaEntity u2_c5_cl3 = commentLikeJpaRepository.save(TestEntityFactory.createCommentLike(u3_v2_c5, otherHostUser2));
        PostLikeJpaEntity u3_v2_pl5 = postLikeJpaRepository.save(TestEntityFactory.createPostLike(otherMemberUser3, u1_v2)); //유저3이 투표2에 좋아요

        // 유저 1이 기록2 작성
        RecordJpaEntity u1_r2 = recordJpaRepository.save(
                RecordJpaEntity.builder().content("유저1이 방2에 생성한 기록2").userJpaEntity(testUser1).page(22).isOverview(false)
                        .commentCount(1).likeCount(1).roomJpaEntity(roomInProgress2).build());
        CommentJpaEntity u2_r2_c6 = commentJpaRepository.save(
                CommentJpaEntity.builder().content("유저2가 기록2에 남긴 댓글6").postJpaEntity(u1_r2).userJpaEntity(otherHostUser2)
                        .likeCount(1).reportCount(0).postType(RECORD).build()); //유저2가 기록2에 댓글
        //유저3이 댓글6에 댓글좋아요
        CommentLikeJpaEntity u3_c6_cl4 = commentLikeJpaRepository.save(TestEntityFactory.createCommentLike(u2_r2_c6, otherMemberUser3));
        PostLikeJpaEntity u2_r2_pl6 = postLikeJpaRepository.save(TestEntityFactory.createPostLike(otherHostUser2, u1_r2)); //유저2가 기록2에 좋아요

        //when
        String accessToken = jwtUtil.createAccessToken(testUser1.getUserId());
        mockMvc.perform(delete("/users")
                .header("Authorization", "Bearer " + accessToken)  //헤더 추가
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // then: 1) 유저 팔로잉/팔로워 관계 삭제
        // 탈퇴한 유저1의 팔로잉/팔로워 관계는 모두 삭제되어야하고, 관련 없는 유저3->유저2 팔로우관계만 남아있어야함
        // 유저2의 팔로워 수가 1이어야함
        assertTrue(followingJpaRepository.findById(u1_u2_f1.getFollowingId()).isEmpty());
        assertTrue(followingJpaRepository.findById(u3_u1_f2.getFollowingId()).isEmpty());
        UserJpaEntity updateUser2 = userJpaRepository.findById(otherHostUser2.getUserId()).orElse(null);
        assertEquals(1, updateUser2.getFollowerCount());
        // 2) 최근검색어 삭제
        assertTrue(recentSearchJpaRepository.findAll().isEmpty());

        // 3) 저장한 책/파드 삭제
        assertTrue(savedFeedJpaRepository.findAllByUserId(testUser1.getUserId()).isEmpty());
        assertTrue(savedBookJpaRepository.findAll().isEmpty());

        // 4) 오늘의 한마디 관계 soft delete (status=INACTIVE)
        AttendanceCheckJpaEntity deletedAc = attendanceCheckJpaRepository.findById(a.getAttendanceCheckId()).orElse(null);
        assertThat(deletedAc.getStatus()).isEqualTo(INACTIVE);

        // 5) 유저 투표 참여 관계 삭제
        // 탈퇴한 유저가 참여한 투표관계 모두 삭제
        // 탈퇴한 유저가 참여했던 투표의 득표수 감소
        assertTrue(voteParticipantJpaRepository.findById(u1_vp1.getVoteParticipantId()).isEmpty());
        VoteItemJpaEntity updateVi1 = voteItemJpaRepository.findById(v1_vi1.getVoteItemId()).orElse(null);
        assertEquals(0, updateVi1.getCount());

        // 6) 유저 댓글 좋아요 삭제
        // 탈퇴한 유저의 모든 댓글 좋아요 관계 삭제
        // 탈퇴한 유저가 좋아요한 댓글의 좋아요 수 감소
        assertTrue(commentLikeJpaRepository.findById(u1_c1_cl1.getLikeId()).isEmpty());
        CommentJpaEntity updatedCm1 = commentJpaRepository.findById(u2_v1_c1.getCommentId()).orElse(null);
        assertEquals(0, updatedCm1.getLikeCount());

        // 7) 유저 댓글 soft delete (status=INACTIVE)
        // 탈퇴한 유저의 모든 댓글 soft delete
        // 탈퇴한 유저가 남긴 게시글의 댓글 수 감소
        // 탈퇴한 유저의 모든 댓글의 좋아요 관계 삭제
        CommentJpaEntity deletedCm1 = commentJpaRepository.findById(u1_v1_c2.getCommentId()).orElse(null);
        CommentJpaEntity deletedCm2 = commentJpaRepository.findById(u1_f1_c3.getCommentId()).orElse(null);
        CommentJpaEntity deletedCm3 = commentJpaRepository.findById(u1_r1_c4.getCommentId()).orElse(null);
        assertThat(deletedCm1.getStatus()).isEqualTo(INACTIVE);
        assertThat(deletedCm2.getStatus()).isEqualTo(INACTIVE);
        assertThat(deletedCm3.getStatus()).isEqualTo(INACTIVE);

        VoteJpaEntity updateV1 = voteJpaRepository.findByPostId(u2_v1.getPostId()).orElse(null);
        FeedJpaEntity updateF1 = feedJpaRepository.findByPostId(u2_f1.getPostId()).orElse(null);
        RecordJpaEntity updateR1 = recordJpaRepository.findByPostId(u2_r1.getPostId()).orElse(null);
        assertEquals(1, updateV1.getCommentCount()); // 유저2가 남긴 댓글은 남아있음
        assertEquals(0, updateF1.getCommentCount());
        assertEquals(0, updateR1.getCommentCount());

        assertTrue(commentLikeJpaRepository.findById(u2_c2_cl.getLikeId()).isEmpty());
        assertTrue(commentLikeJpaRepository.findById(u2_c3_cl.getLikeId()).isEmpty());
        assertTrue(commentLikeJpaRepository.findById(u2_c4_cl.getLikeId()).isEmpty());

        // 8) 유저 게시글 좋아요 삭제
        // 탈퇴한 유저가 남긴 모든 게시글 좋아요 삭제
        // 탈퇴한 유저가 남긴 게시글의 좋아요 수 감소
        assertTrue(postLikeJpaRepository.findById(u1_v1_pl1.getLikeId()).isEmpty());
        assertTrue(postLikeJpaRepository.findById(u1_f1_pl2.getLikeId()).isEmpty());
        assertTrue(postLikeJpaRepository.findById(u1_r1_pl3.getLikeId()).isEmpty());
        assertEquals(0, updateV1.getLikeCount());
        assertEquals(0, updateF1.getLikeCount());
        assertEquals(0, updateR1.getLikeCount());

        // 9) 유저가 작성한 피드 soft delete (status=INACTIVE)
        // 탈퇴한 유저가 작성한 모든 피드 soft delete
        // 탈퇴한 유저가 작성한 모든 피드의 댓글 좋아요 삭제
        // 탈퇴한 유저가 작성한 모든 피드의 댓글 soft delete
        // 탈퇴한 유저가 작성한 모든 피드 좋아요 삭제
        // 탈퇴한 유저가 작성한 피드를 저장하는 모든 관계 삭제
        FeedJpaEntity deletedF1 = feedJpaRepository.findById(u1_f2.getPostId()).orElse(null);
        assertThat(deletedF1.getStatus()).isEqualTo(INACTIVE);
        assertTrue(commentLikeJpaRepository.findById(u3_c4_cl2.getLikeId()).isEmpty());
        CommentJpaEntity deletedCm4 = commentJpaRepository.findById(u2_f3_c4.getCommentId()).orElse(null);
        assertThat(deletedCm4.getStatus()).isEqualTo(INACTIVE);
        assertTrue(postLikeJpaRepository.findById(u2_f2_pl4.getLikeId()).isEmpty());
        assertTrue(savedFeedJpaRepository.findAllByUserId(otherHostUser2.getUserId()).isEmpty());

        // 9) 유저가 작성한 투표 soft delete (status=INACTIVE)
        // 탈퇴한 유저가 작성한 모든 투표 soft delete
        // 탈퇴한 유저가 작성한 모든 투표의 댓글 좋아요 삭제
        // 탈퇴한 유저가 작성한 모든 투표의 댓글 soft delete
        // 탈퇴한 유저가 작성한 모든 투표 좋아요 삭제
        // 탈퇴한 유저가 작성한 투표에 참여하는 모든 관계를 삭제
        // 탈퇴한 유저가 작성한 모든 투표의 투표 항목 삭제
        VoteJpaEntity deletedV1 = voteJpaRepository.findById(u1_v2.getPostId()).orElse(null);
        assertThat(deletedV1.getStatus()).isEqualTo(INACTIVE);
        assertTrue(commentLikeJpaRepository.findById(u2_c5_cl3.getLikeId()).isEmpty());
        CommentJpaEntity deletedCm5 = commentJpaRepository.findById(u3_v2_c5.getCommentId()).orElse(null);
        assertThat(deletedCm5.getStatus()).isEqualTo(INACTIVE);
        assertTrue(postLikeJpaRepository.findById(u3_v2_pl5.getLikeId()).isEmpty());
        assertTrue(voteParticipantJpaRepository.findById(u2_vp2.getVoteParticipantId()).isEmpty());
        assertTrue(voteParticipantJpaRepository.findById(u3_vp3.getVoteParticipantId()).isEmpty());
        assertTrue(voteItemJpaRepository.findById(v2_vi1.getVoteItemId()).isEmpty());
        assertTrue(voteItemJpaRepository.findById(v2_vi2.getVoteItemId()).isEmpty());

        // 10) 유저가 작성한 기록 soft delete (status=INACTIVE)
        // 탈퇴한 유저가 작성한 모든 기록 soft delete
        // 탈퇴한 유저가 작성한 모든 기록의 댓글 좋아요 삭제
        // 탈퇴한 유저가 작성한 모든 기록의 댓글 soft delete
        // 탈퇴한 유저가 작성한 모든 기록 좋아요 삭제
        RecordJpaEntity deletedR1 = recordJpaRepository.findById(u1_r2.getPostId()).orElse(null);
        assertThat(deletedR1.getStatus()).isEqualTo(INACTIVE);
        assertTrue(commentLikeJpaRepository.findById(u3_c6_cl4.getLikeId()).isEmpty());
        CommentJpaEntity deletedCm6 = commentJpaRepository.findById(u2_r2_c6.getCommentId()).orElse(null);
        assertThat(deletedCm6.getStatus()).isEqualTo(INACTIVE);
        assertTrue(postLikeJpaRepository.findById(u2_r2_pl6.getLikeId()).isEmpty());

        // 11) 유저가 참여한 방 관계 soft delete (status=INACTIVE)
        // 탈퇴한 유저가 참여한 모든 방 관계 soft delete
        // 탈퇴한 유저가 참여한 모든 방의 멤버수 감소
        // 탈퇴한 유저가 참여한 모든 방의 진행도 업데이트
        RoomParticipantJpaEntity deletedRp1 = roomParticipantJpaRepository.
                findById(r1_rp1.getRoomParticipantId()).orElse(null); //만료된 방에서는 host가 나가도 상관없음
        RoomParticipantJpaEntity deletedRp2 = roomParticipantJpaRepository.
                findById(r2_rp1.getRoomParticipantId()).orElse(null);
        RoomParticipantJpaEntity deletedRp3 = roomParticipantJpaRepository.
                findById(r3_rp1.getRoomParticipantId()).orElse(null);
        assertThat(deletedRp1.getStatus()).isEqualTo(INACTIVE);
        assertThat(deletedRp2.getStatus()).isEqualTo(INACTIVE);
        assertThat(deletedRp3.getStatus()).isEqualTo(INACTIVE);
        assertEquals(2, roomJpaRepository.findByRoomId(roomExpired1.getRoomId()).get().getMemberCount());
        assertEquals(45.0, roomJpaRepository.findByRoomId(roomExpired1.getRoomId()).get().getRoomPercentage());
        assertEquals(2, roomJpaRepository.findByRoomId(roomInProgress2.getRoomId()).get().getMemberCount());
        assertEquals(45.0, roomJpaRepository.findByRoomId(roomInProgress2.getRoomId()).get().getRoomPercentage());
        assertEquals(2, roomJpaRepository.findByRoomId(roomRecruiting3.getRoomId()).get().getMemberCount());
        assertEquals(45.0, roomJpaRepository.findByRoomId(roomRecruiting3.getRoomId()).get().getRoomPercentage());

        // 12) 유저 soft delete (status=INACTIVE)
        // 탈퇴한 유저의 oauth2Id는 deleted:로 시작해야함
        entityManager.clear();
        UserJpaEntity deletedUser = userJpaRepository.findById(testUser1.getUserId()).orElse(null);
        assertThat(deletedUser.getStatus()).isEqualTo(INACTIVE);
        assertThat(deletedUser.getOauth2Id()).startsWith("deleted:");

        // 13) 탈퇴한 유저의 토큰을 블랙리스트 등록 검증
        assertTrue(userTokenBlacklistQueryPort.isTokenBlacklisted(accessToken));
    }

    @Test
    @DisplayName("회원탈퇴 시 진행/모집 중인 방의 호스트라면 [400 에러 발생]")
    void deleteUser_whenHostInActiveRoom_thenThrowBusinessException() throws Exception {

        // given
        UserJpaEntity hostUser1 = userJpaRepository.save(TestEntityFactory.createUser(Alias.ARTIST));
        BookJpaEntity book = bookJpaRepository.save(TestEntityFactory.createBook());
        Category category = TestEntityFactory.createLiteratureCategory();
        RoomJpaEntity roomInProgress = createRoom(book, category, IN_PROGRESS);
        roomParticipantJpaRepository.save(TestEntityFactory.createRoomParticipant(roomInProgress, hostUser1, HOST, 100.0));

        // when
        String accessToken1 = jwtUtil.createAccessToken(hostUser1.getUserId());
        // then
        mockMvc.perform(delete("/users")
                        .header("Authorization", "Bearer " + accessToken1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(USER_CANNOT_DELETE_ROOM_HOST.getCode()));

        // given
        UserJpaEntity hostUser2 = userJpaRepository.save(TestEntityFactory.createUser(Alias.ARTIST));
        RoomJpaEntity roomInRecruiting = createRoom(book, category, RECRUITING);
        roomParticipantJpaRepository.save(TestEntityFactory.createRoomParticipant(roomInRecruiting, hostUser2, HOST, 100.0));

        // when
        String accessToken2 = jwtUtil.createAccessToken(hostUser2.getUserId());

        // then
        mockMvc.perform(delete("/users")
                        .header("Authorization", "Bearer " + accessToken2)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(USER_CANNOT_DELETE_ROOM_HOST.getCode()));

    }


    private RoomJpaEntity createRoom(BookJpaEntity book, Category category, RoomStatus roomStatus) {
        return roomJpaRepository.save(RoomJpaEntity.builder()
                .title("방이름")
                .description("설명")
                .isPublic(true)
                .startDate(LocalDate.now().minusDays(1))
                .endDate(LocalDate.now().plusDays(30))
                .recruitCount(3)
                .bookJpaEntity(book)
                .category(category)
                .memberCount(3) // 방장과 참여자 포함
                .roomStatus(roomStatus)
                .build());
    }

    private void updateRoomPercentage(RoomParticipantJpaEntity roomParticipant1, RoomParticipantJpaEntity roomParticipant2,
                                      RoomParticipantJpaEntity roomParticipant3,RoomJpaEntity room) {
        roomParticipant1.updateCurrentPage(50); // 탈퇴하는 유저의 진행도 50
        roomParticipantJpaRepository.save(roomParticipant1);

        roomParticipant2.updateCurrentPage(30); // 2번째 유저의 진행도 30;
        roomParticipantJpaRepository.save(roomParticipant2);

        roomParticipant3.updateCurrentPage(60); // 3번째 유저의 진행도 60
        roomParticipantJpaRepository.save(roomParticipant3);

        room.updateRoomPercentage(46.6); // 방참여자들의 진행도 평균 46.6 --> 유저1이 탈퇴하면 진행도 평균은 45
        roomJpaRepository.save(room);
    }
}
