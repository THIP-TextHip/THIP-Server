package konkuk.thip.room.adapter.in.web;

import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.room.adapter.out.jpa.CategoryJpaEntity;
import konkuk.thip.room.adapter.out.jpa.RoomJpaEntity;
import konkuk.thip.room.adapter.out.persistence.repository.CategoryJpaRepository;
import konkuk.thip.room.adapter.out.persistence.repository.RoomJpaRepository;
import konkuk.thip.user.adapter.out.jpa.*;
import konkuk.thip.user.adapter.out.persistence.repository.AliasJpaRepository;
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

import java.time.LocalDate;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("[통합] 모임 홈 참여중인 내 모임방 조회 api 통합 테스트")
class RoomGetHomeJoinedRoomsApiTest {

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

    private RoomJpaEntity room1;
    private RoomJpaEntity room2;
    private UserJpaEntity user1;
    private UserJpaEntity user2;
    private BookJpaEntity book;
    private CategoryJpaEntity category;

    @BeforeEach
    void setUp() {

        AliasJpaEntity alias = TestEntityFactory.createLiteratureAlias();
        aliasJpaRepository.save(alias);

        user1 = userJpaRepository.save(TestEntityFactory.createUser(alias));
        user2 = userJpaRepository.save(TestEntityFactory.createUser(alias));

        book = TestEntityFactory.createBook();
        bookJpaRepository.save(book);

        category = TestEntityFactory.createLiteratureCategory(alias);
        categoryJpaRepository.save(category);

        room1 = roomJpaRepository.save(TestEntityFactory.createRoom(book, category));
        room2 = roomJpaRepository.save(TestEntityFactory.createRoom(book, category));

        // 1번방에 유저 1이 호스트, 유저2가 멤버
        userRoomJpaRepository.save(TestEntityFactory.createUserRoom(room1,user1,UserRoomRole.HOST, 80.0));
        userRoomJpaRepository.save(TestEntityFactory.createUserRoom(room1,user2,UserRoomRole.MEMBER, 60.0));

        // 2번방에 유저 1이 호스트
        userRoomJpaRepository.save(TestEntityFactory.createUserRoom(room2,user1,UserRoomRole.HOST,60.0));
    }

    @AfterEach
    void tearDown() {
        userRoomJpaRepository.deleteAll();
        roomJpaRepository.deleteAll();
        bookJpaRepository.deleteAll();
        userJpaRepository.deleteAll();
        categoryJpaRepository.deleteAll();
        aliasJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("사용자가 참여중인 방 목록이 현재 진행되고 있는 방 중에서 진행률 내림차순, 시작일 오름차순으로 조회된다.")
    void getHomeJoinedRooms_success() throws Exception {

        //given
        Long userId = user1.getUserId();

        //when
        ResultActions result = mockMvc.perform(get("/rooms/home/joined")
                .requestAttr("userId", userId)
                .param("page", "1"));

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roomList", hasSize(2)))
                .andExpect(jsonPath("$.data.nickname").exists())
                .andExpect(jsonPath("$.data.page", is(1)))
                .andExpect(jsonPath("$.data.first", is(true)))
                .andExpect(jsonPath("$.data.last", is(true)))
                // 진행률 내림차순, 시작일 오름차순 정렬 검증
                .andExpect(jsonPath("$.data.roomList[0].userPercentage", is(80)))
                .andExpect(jsonPath("$.data.roomList[1].userPercentage", is(60)))
                .andExpect(jsonPath("$.data.roomList[0].roomId").exists())
                .andExpect(jsonPath("$.data.roomList[1].roomId").exists());
    }

    @Test
    @DisplayName("사용자가 참여중인 방 목록 중 현재 진행되고 있는 방 중에서 진행률이 같을 때 시작일이 빠른 방이 먼저 조회된다.")
    void getHomeJoinedRooms_sortByStartDateWhenUserPercentageEquals() throws Exception {

        // given
        AliasJpaEntity alias = TestEntityFactory.createLiteratureAlias();
        aliasJpaRepository.save(alias);

        UserJpaEntity newUser = userJpaRepository.save(TestEntityFactory.createUser(alias));

        // 방1: 시작일 오늘-2
        RoomJpaEntity room1 = roomJpaRepository.save(
                TestEntityFactory.createCustomRoom(book, category, LocalDate.now().minusDays(2), LocalDate.now().plusDays(10))
        );
        // 방2: 시작일 오늘-1
        RoomJpaEntity room2 = roomJpaRepository.save(
                TestEntityFactory.createCustomRoom(book, category, LocalDate.now().minusDays(1), LocalDate.now().plusDays(8))
        );
        // 방3: 시작일 오늘
        RoomJpaEntity room3 = roomJpaRepository.save(
                TestEntityFactory.createCustomRoom(book, category, LocalDate.now(), LocalDate.now().plusDays(9))
        );

        // 모두 동일한 진행률(70%)로 참여
        userRoomJpaRepository.save(TestEntityFactory.createUserRoom(room1, newUser, UserRoomRole.MEMBER, 70.0));
        userRoomJpaRepository.save(TestEntityFactory.createUserRoom(room2, newUser, UserRoomRole.MEMBER, 70.0));
        userRoomJpaRepository.save(TestEntityFactory.createUserRoom(room3, newUser, UserRoomRole.MEMBER, 70.0));

        Long userId = newUser.getUserId();

        // when
        ResultActions result = mockMvc.perform(get("/rooms/home/joined")
                .requestAttr("userId", userId)
                .param("page", "1"));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roomList", hasSize(3)))
                // 모두 진행률 70%
                .andExpect(jsonPath("$.data.roomList[0].userPercentage", is(70)))
                .andExpect(jsonPath("$.data.roomList[1].userPercentage", is(70)))
                .andExpect(jsonPath("$.data.roomList[2].userPercentage", is(70)))
                // 시작일 빠른 순서로 정렬되었는지 검증 (room1 → room2 → room3)
                .andExpect(jsonPath("$.data.roomList[0].roomId", is(room1.getRoomId().intValue())))
                .andExpect(jsonPath("$.data.roomList[1].roomId", is(room2.getRoomId().intValue())))
                .andExpect(jsonPath("$.data.roomList[2].roomId", is(room3.getRoomId().intValue())));
    }

    @Test
    @DisplayName("사용자가 참여중인 방 목록 중 모집중(시작 전)인 방은 참여중 목록에 포함되지 않는다.")
    void getHomeJoinedRooms_excludeRecruitingRooms() throws Exception {

        // given
        AliasJpaEntity alias = TestEntityFactory.createLiteratureAlias();
        aliasJpaRepository.save(alias);

        UserJpaEntity newUser = userJpaRepository.save(TestEntityFactory.createUser(alias));

        // 모집중(시작일 미래)
        RoomJpaEntity recruitRoom = roomJpaRepository.save(
                TestEntityFactory.createCustomRoom(book, category, LocalDate.now().plusDays(2), LocalDate.now().plusDays(5))
        );
        // 활동중(시작일 오늘-1, 종료일 오늘+2)
        RoomJpaEntity activeRoom = roomJpaRepository.save(
                TestEntityFactory.createCustomRoom(book, category, LocalDate.now().minusDays(1), LocalDate.now().plusDays(2))
        );

        userRoomJpaRepository.save(TestEntityFactory.createUserRoom(recruitRoom, newUser, UserRoomRole.MEMBER, 20.0));
        userRoomJpaRepository.save(TestEntityFactory.createUserRoom(activeRoom, newUser, UserRoomRole.MEMBER, 50.0));


        // when
        ResultActions result = mockMvc.perform(get("/rooms/home/joined")
                .requestAttr("userId", newUser.getUserId())
                .param("page", "1"));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roomList", hasSize(1)))
                .andExpect(jsonPath("$.data.roomList[0].roomId", is(activeRoom.getRoomId().intValue())))
                .andExpect(jsonPath("$.data.roomList[0].userPercentage", is(50)));
    }


    @Test
    @DisplayName("사용자가 참여중인 방이 없으면 빈 리스트를 반환한다.")
    void getHomeJoinedRooms_empty() throws Exception {

        //given
        AliasJpaEntity alias = TestEntityFactory.createLiteratureAlias();
        aliasJpaRepository.save(alias);
        UserJpaEntity newUser = userJpaRepository.save(TestEntityFactory.createUser(alias));

        //when
        ResultActions result = mockMvc.perform(get("/rooms/home/joined")
                .requestAttr("userId", newUser.getUserId())
                .param("page", "1"));

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roomList", hasSize(0)))
                .andExpect(jsonPath("$.data.page", is(1)))
                .andExpect(jsonPath("$.data.first", is(true)))
                .andExpect(jsonPath("$.data.last", is(true)));
    }

}
