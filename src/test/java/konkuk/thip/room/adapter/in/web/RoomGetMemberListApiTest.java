package konkuk.thip.room.adapter.in.web;

import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.room.adapter.out.jpa.RoomJpaEntity;
import konkuk.thip.room.adapter.out.jpa.RoomParticipantRole;
import konkuk.thip.room.adapter.out.persistence.repository.RoomJpaRepository;
import konkuk.thip.room.adapter.out.persistence.repository.roomparticipant.RoomParticipantJpaRepository;
import konkuk.thip.room.domain.value.Category;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserRole;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.user.adapter.out.persistence.repository.following.FollowingJpaRepository;
import konkuk.thip.user.domain.value.Alias;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("[통합] 독서 메이트(방 멤버) 조회 api 통합 테스트")
class RoomGetMemberListApiTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserJpaRepository userJpaRepository;
    @Autowired private BookJpaRepository bookJpaRepository;
    @Autowired private RoomJpaRepository roomJpaRepository;
    @Autowired private RoomParticipantJpaRepository roomParticipantJpaRepository;
    @Autowired private FollowingJpaRepository followingJpaRepository;

    private RoomJpaEntity room1;
    private UserJpaEntity user1;
    private UserJpaEntity user2;
    private UserJpaEntity user3;
    private BookJpaEntity book;
    private Category category;

    @BeforeEach
    void setUp() {
        Alias alias = TestEntityFactory.createLiteratureAlias();
        user1 = userJpaRepository.save(UserJpaEntity.builder()
                .nickname("테스터1")
                .nicknameUpdatedAt(LocalDate.now().minusMonths(7))
                .oauth2Id("kakao_1")
                .alias(alias)
                .role(UserRole.USER)
                .followerCount(2) // user1이 user2, user3를 팔로우
                .build());

        user2 = userJpaRepository.save(UserJpaEntity.builder()
                .nickname("테스터2")
                .nicknameUpdatedAt(LocalDate.now().minusMonths(7))
                .oauth2Id("kakao_2")
                .alias(alias)
                .role(UserRole.USER)
                .followerCount(1) // user2가 user3를 팔로우
                .build());

        user3 = userJpaRepository.save(UserJpaEntity.builder()
                .nickname("테스터3")
                .nicknameUpdatedAt(LocalDate.now().minusMonths(7))
                .oauth2Id("kakao_3")
                .alias(alias)
                .role(UserRole.USER)
                .followerCount(1) // user3가 user1을 팔로우
                .build());

        book = bookJpaRepository.save(TestEntityFactory.createBook());
        category = TestEntityFactory.createLiteratureCategory();

        room1 = roomJpaRepository.save(TestEntityFactory.createRoom(book, category));

        // 유저1(호스트), 유저2(멤버), 유저3(멤버)로 참여
        roomParticipantJpaRepository.save(TestEntityFactory.createRoomParticipant(room1, user1, RoomParticipantRole.HOST, 80.0));
        roomParticipantJpaRepository.save(TestEntityFactory.createRoomParticipant(room1, user2, RoomParticipantRole.MEMBER, 60.0));
        roomParticipantJpaRepository.save(TestEntityFactory.createRoomParticipant(room1, user3, RoomParticipantRole.MEMBER, 50.0));


        // 팔로잉 관계 설정
        // user1이 user2, user3을 팔로우
        followingJpaRepository.save(TestEntityFactory.createFollowing(user1, user2));
        followingJpaRepository.save(TestEntityFactory.createFollowing(user1, user3));
        // user2가 user3을 팔로우
        followingJpaRepository.save(TestEntityFactory.createFollowing(user2, user3));
        // user3이 user1을 팔로우
        followingJpaRepository.save(TestEntityFactory.createFollowing(user3, user1));
    }

    @AfterEach
    void tearDown() {
        followingJpaRepository.deleteAllInBatch();
        roomParticipantJpaRepository.deleteAllInBatch();
        roomJpaRepository.deleteAll();
        bookJpaRepository.deleteAll();
        userJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("방 멤버 리스트(독서메이트)가 userId, nickname, imageUrl, alias, subscriberCount로 조회된다.")
    void getRoomMemberList_success() throws Exception {
        //given
        //room1에 user1,user2,user3가 참여
        Long roomId = room1.getRoomId();

        //when
        ResultActions result = mockMvc.perform(get("/rooms/{roomId}/users", roomId)
                .requestAttr("userId", user1.getUserId()));

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userList", hasSize(3)))
                .andExpect(jsonPath("$.data.userList[0].userId").value(user1.getUserId().intValue()))
                .andExpect(jsonPath("$.data.userList[0].nickname").exists())
                .andExpect(jsonPath("$.data.userList[0].imageUrl").exists())
                .andExpect(jsonPath("$.data.userList[0].aliasName").exists())
                .andExpect(jsonPath("$.data.userList[0].followerCount").isNumber())
                .andExpect(jsonPath("$.data.userList[1].userId").value(user2.getUserId().intValue()))
                .andExpect(jsonPath("$.data.userList[1].nickname").exists())
                .andExpect(jsonPath("$.data.userList[1].imageUrl").exists())
                .andExpect(jsonPath("$.data.userList[1].aliasName").exists())
                .andExpect(jsonPath("$.data.userList[1].followerCount").isNumber())
                .andExpect(jsonPath("$.data.userList[2].userId").value(user3.getUserId().intValue()))
                .andExpect(jsonPath("$.data.userList[2].nickname").exists())
                .andExpect(jsonPath("$.data.userList[2].imageUrl").exists())
                .andExpect(jsonPath("$.data.userList[2].aliasName").exists())
                .andExpect(jsonPath("$.data.userList[2].followerCount").isNumber());
    }

    @Test
    @DisplayName("방에 본인을 제외한 다른 멤버가 없으면, 반환되는 독서메이트는 1명(= 본인 혼자) 이다.")
    void getRoomMemberList_just_me_alone_participant() throws Exception {
        //given : me 가 방에 혼자 있는 상황
        RoomJpaEntity userAloneRoom = roomJpaRepository.save(TestEntityFactory.createRoom(book, category));
        UserJpaEntity me = userJpaRepository.save(TestEntityFactory.createUser(Alias.WRITER));
        roomParticipantJpaRepository.save(TestEntityFactory.createRoomParticipant(userAloneRoom, me, RoomParticipantRole.HOST, 0.0));

        //when
        ResultActions result = mockMvc.perform(get("/rooms/{roomId}/users", userAloneRoom.getRoomId())
                .requestAttr("userId", me.getUserId()));

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userList", hasSize(1)));    // me
    }

    @Test
    @DisplayName("팔로워 수가 올바르게 집계된다.")
    void getRoomMemberList_subscriberCount() throws Exception {
        //given
        Long roomId = room1.getRoomId();

        //when
        ResultActions result = mockMvc.perform(get("/rooms/{roomId}/users", roomId)
                .requestAttr("userId", user1.getUserId()));

        //then
        // user1이 팔로우하는 사람: user2, user3 (2명)
        // user2가 팔로우하는 사람: user3 (1명)
        // user3이 팔로우하는 사람: user1 (1명)
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userList[?(@.userId==" + user1.getUserId() + ")].followerCount").value(contains(2)))
                .andExpect(jsonPath("$.data.userList[?(@.userId==" + user2.getUserId() + ")].followerCount").value(contains(1)))
                .andExpect(jsonPath("$.data.userList[?(@.userId==" + user3.getUserId() + ")].followerCount").value(contains(1)));
    }

    @Test
    @DisplayName("팔로워가 한 명도 없는 사용자는 followerCount가 0으로 조회된다.")
    void getRoomMemberList_noSubscriber() throws Exception {
        //given
        UserJpaEntity userNoFollower = userJpaRepository.save(TestEntityFactory.createUser(Alias.WRITER));
        roomParticipantJpaRepository.save(TestEntityFactory.createRoomParticipant(room1, userNoFollower, RoomParticipantRole.MEMBER, 10.0));
        Long roomId = room1.getRoomId();

        //when
        ResultActions result = mockMvc.perform(get("/rooms/{roomId}/users", roomId)
                .requestAttr("userId", user1.getUserId()));

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userList[?(@.userId==" + userNoFollower.getUserId() + ")].followerCount").value(contains(0)));
    }
}
