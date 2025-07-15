package konkuk.thip.room.adapter.in.web;

import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.room.adapter.out.jpa.CategoryJpaEntity;
import konkuk.thip.room.adapter.out.jpa.RoomJpaEntity;
import konkuk.thip.room.adapter.out.persistence.repository.CategoryJpaRepository;
import konkuk.thip.room.adapter.out.persistence.repository.RoomJpaRepository;
import konkuk.thip.user.adapter.out.jpa.AliasJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserRoomRole;
import konkuk.thip.user.adapter.out.persistence.repository.AliasJpaRepository;
import konkuk.thip.user.adapter.out.persistence.repository.FollowingJpaRepository;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.user.adapter.out.persistence.repository.UserRoomJpaRepository;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("[통합] 독서 메이트(방 멤버) 조회 api 통합 테스트")
class RoomGetMemberListApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AliasJpaRepository aliasJpaRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private CategoryJpaRepository categoryJpaRepository;

    @Autowired
    private BookJpaRepository bookJpaRepository;

    @Autowired
    private RoomJpaRepository roomJpaRepository;

    @Autowired
    private UserRoomJpaRepository userRoomJpaRepository;

    @Autowired
    private FollowingJpaRepository followingJpaRepository;

    private RoomJpaEntity room1;
    private UserJpaEntity user1;
    private UserJpaEntity user2;
    private UserJpaEntity user3;
    private BookJpaEntity book;
    private CategoryJpaEntity category;

    @BeforeEach
    void setUp() {
        AliasJpaEntity alias = TestEntityFactory.createLiteratureAlias();
        aliasJpaRepository.save(alias);

        user1 = userJpaRepository.save(TestEntityFactory.createUser(alias));
        user2 = userJpaRepository.save(TestEntityFactory.createUser(alias));
        user3 = userJpaRepository.save(TestEntityFactory.createUser(alias));

        book = bookJpaRepository.save(TestEntityFactory.createBook());
        category = categoryJpaRepository.save(TestEntityFactory.createLiteratureCategory(alias));

        room1 = roomJpaRepository.save(TestEntityFactory.createRoom(book, category));

        // 유저1(호스트), 유저2(멤버), 유저3(멤버)로 참여
        userRoomJpaRepository.save(TestEntityFactory.createUserRoom(room1, user1, UserRoomRole.HOST, 80.0));
        userRoomJpaRepository.save(TestEntityFactory.createUserRoom(room1, user2, UserRoomRole.MEMBER, 60.0));
        userRoomJpaRepository.save(TestEntityFactory.createUserRoom(room1, user3, UserRoomRole.MEMBER, 50.0));


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
        userRoomJpaRepository.deleteAll();
        roomJpaRepository.deleteAll();
        bookJpaRepository.deleteAll();
        userJpaRepository.deleteAll();
        categoryJpaRepository.deleteAll();
        aliasJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("방 멤버 리스트(독서메이트)가 userId, nickname, imageUrl, alias, subscriberCount로 조회된다.")
    void getRoomMemberList_success() throws Exception {
        //given
        //room1에 user1,user2,user3가 참여
        Long roomId = room1.getRoomId();

        //when
        ResultActions result = mockMvc.perform(get("/rooms/{roomId}/users", roomId));

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userList", hasSize(3)))
                .andExpect(jsonPath("$.data.userList[0].userId").value(user1.getUserId().intValue()))
                .andExpect(jsonPath("$.data.userList[0].nickname").exists())
                .andExpect(jsonPath("$.data.userList[0].imageUrl").exists())
                .andExpect(jsonPath("$.data.userList[0].alias").exists())
                .andExpect(jsonPath("$.data.userList[0].subscriberCount").isNumber())
                .andExpect(jsonPath("$.data.userList[1].userId").value(user2.getUserId().intValue()))
                .andExpect(jsonPath("$.data.userList[1].nickname").exists())
                .andExpect(jsonPath("$.data.userList[1].imageUrl").exists())
                .andExpect(jsonPath("$.data.userList[1].alias").exists())
                .andExpect(jsonPath("$.data.userList[1].subscriberCount").isNumber())
                .andExpect(jsonPath("$.data.userList[2].userId").value(user3.getUserId().intValue()))
                .andExpect(jsonPath("$.data.userList[2].nickname").exists())
                .andExpect(jsonPath("$.data.userList[2].imageUrl").exists())
                .andExpect(jsonPath("$.data.userList[2].alias").exists())
                .andExpect(jsonPath("$.data.userList[2].subscriberCount").isNumber());
    }

    @Test
    @DisplayName("방에 멤버가 없으면 빈 리스트를 반환한다.")
    void getRoomMemberList_empty() throws Exception {
        //given
        RoomJpaEntity emptyRoom = roomJpaRepository.save(TestEntityFactory.createRoom(book, category));

        //when
        ResultActions result = mockMvc.perform(get("/rooms/{roomId}/users", emptyRoom.getRoomId()));

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userList", hasSize(0)));
    }

    @Test
    @DisplayName("팔로워(구독자) 수가 올바르게 집계된다.")
    void getRoomMemberList_subscriberCount() throws Exception {
        //given
        Long roomId = room1.getRoomId();

        //when
        ResultActions result = mockMvc.perform(get("/rooms/{roomId}/users", roomId));

        //then
        // user1: user3이 팔로우(1명)
        // user2: user1이 팔로우(1명)
        // user3: user1, user2가 팔로우(2명)
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userList[?(@.userId==" + user1.getUserId() + ")].subscriberCount").value(contains(1)))
                .andExpect(jsonPath("$.data.userList[?(@.userId==" + user2.getUserId() + ")].subscriberCount").value(contains(1)))
                .andExpect(jsonPath("$.data.userList[?(@.userId==" + user3.getUserId() + ")].subscriberCount").value(contains(2)));
    }

    @Test
    @DisplayName("팔로워가 한 명도 없는 사용자는 subscriberCount가 0으로 조회된다.")
    void getRoomMemberList_noSubscriber() throws Exception {
        //given
        UserJpaEntity userNoFollower = userJpaRepository.save(TestEntityFactory.createUser(aliasJpaRepository.findAll().get(0)));
        userRoomJpaRepository.save(TestEntityFactory.createUserRoom(room1, userNoFollower, UserRoomRole.MEMBER, 10.0));
        Long roomId = room1.getRoomId();

        //when
        ResultActions result = mockMvc.perform(get("/rooms/{roomId}/users", roomId));

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userList[?(@.userId==" + userNoFollower.getUserId() + ")].subscriberCount").value(contains(0)));
    }
}