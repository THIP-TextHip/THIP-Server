package konkuk.thip.room.adapter.in.web;

import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.room.adapter.out.jpa.RoomJpaEntity;
import konkuk.thip.room.adapter.out.jpa.RoomParticipantJpaEntity;
import konkuk.thip.room.domain.value.RoomParticipantRole;
import konkuk.thip.room.adapter.out.persistence.repository.RoomJpaRepository;
import konkuk.thip.room.domain.value.Category;
import konkuk.thip.room.domain.value.RoomStatus;
import konkuk.thip.user.adapter.out.jpa.*;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.room.adapter.out.persistence.repository.roomparticipant.RoomParticipantJpaRepository;
import konkuk.thip.user.domain.value.Alias;
import org.hamcrest.Matchers;
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

    @Autowired private MockMvc mockMvc;
    @Autowired private UserJpaRepository userJpaRepository;
    @Autowired private BookJpaRepository bookJpaRepository;
    @Autowired private RoomJpaRepository roomJpaRepository;
    @Autowired private RoomParticipantJpaRepository roomParticipantJpaRepository;

    private RoomJpaEntity room1;
    private RoomJpaEntity room2;
    private UserJpaEntity user1;
    private UserJpaEntity user2;
    private BookJpaEntity book;
    private Category category;

    @BeforeEach
    void setUp() {

        Alias alias = TestEntityFactory.createLiteratureAlias();
        user1 = userJpaRepository.save(TestEntityFactory.createUser(alias));
        user2 = userJpaRepository.save(TestEntityFactory.createUser(alias));

        book = TestEntityFactory.createBook();
        bookJpaRepository.save(book);

        category = TestEntityFactory.createLiteratureCategory();
        room1 = TestEntityFactory.createRoom(book, category);
        room1.updateRoomStatus(RoomStatus.IN_PROGRESS);
        roomJpaRepository.save(room1);

        room2 = TestEntityFactory.createRoom(book, category);
        room2.updateRoomStatus(RoomStatus.IN_PROGRESS);
        roomJpaRepository.save(room2);

        // 1번방에 유저 1이 호스트, 유저2가 멤버
        roomParticipantJpaRepository.save(TestEntityFactory.createRoomParticipant(room1,user1, RoomParticipantRole.HOST, 80.0));
        roomParticipantJpaRepository.save(TestEntityFactory.createRoomParticipant(room1,user2, RoomParticipantRole.MEMBER, 60.0));

        // 2번방에 유저 1이 호스트
        roomParticipantJpaRepository.save(TestEntityFactory.createRoomParticipant(room2,user1, RoomParticipantRole.HOST,60.0));
    }

    @AfterEach
    void tearDown() {
        roomParticipantJpaRepository.deleteAllInBatch();
        roomJpaRepository.deleteAll();
        bookJpaRepository.deleteAll();
        userJpaRepository.deleteAll();
    }

    private RoomJpaEntity saveScienceRoom(String bookTitle, String isbn, String roomName, LocalDate startDate, LocalDate endDate, int recruitCount) {
        BookJpaEntity book = bookJpaRepository.save(BookJpaEntity.builder()
                .title(bookTitle)
                .isbn(isbn)
                .authorName("한강")
                .bestSeller(false)
                .publisher("문학동네")
                .imageUrl("https://image1.jpg")
                .pageCount(300)
                .description("한강의 소설")
                .build());

        Category category = TestEntityFactory.createScienceCategory();

        return roomJpaRepository.save(RoomJpaEntity.builder()
                .title(roomName)
                .description("한강 작품 읽기 모임")
                .isPublic(true)
                .roomPercentage(0.0)
                .startDate(startDate)
                .endDate(endDate)
                .recruitCount(recruitCount)
                .bookJpaEntity(book)
                .category(category)
                .build());
    }

    private void changeRoomMemberCount(RoomJpaEntity roomJpaEntity, int count) {
        roomJpaEntity.updateMemberCount(count);
        roomJpaRepository.save(roomJpaEntity);
    }

    private void saveSingleUserToRoom(RoomJpaEntity roomJpaEntity, UserJpaEntity userJpaEntity, Double userPercentage) {
        RoomParticipantJpaEntity roomParticipantJpaEntity = RoomParticipantJpaEntity.builder()
                .userJpaEntity(userJpaEntity)
                .roomJpaEntity(roomJpaEntity)
                .roomParticipantRole(RoomParticipantRole.HOST)
                .userPercentage(userPercentage)
                .build();
        roomParticipantJpaRepository.save(roomParticipantJpaEntity);

        roomJpaEntity.updateMemberCount(roomJpaEntity.getMemberCount() + 1);
        roomJpaRepository.save(roomJpaEntity);

    }

    @Test
    @DisplayName("사용자가 참여중인 방 목록이 현재 진행되고 있는 방 중에서 진행률 내림차순, 시작일 오름차순으로 조회된다.")
    void getHomeJoinedRooms_success() throws Exception {

        //given
        Long userId = user1.getUserId();

        //when
        ResultActions result = mockMvc.perform(get("/rooms/home/joined")
                .requestAttr("userId", userId));

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roomList", hasSize(2)))
                .andExpect(jsonPath("$.data.nickname").exists())
                .andExpect(jsonPath("$.data.nextCursor").value(Matchers.nullValue()))
                .andExpect(jsonPath("$.data.isLast", is(true)))
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
        Alias alias = TestEntityFactory.createLiteratureAlias();
        UserJpaEntity newUser = userJpaRepository.save(TestEntityFactory.createUser(alias));

        // 방1: 시작일 오늘-2
        RoomJpaEntity room1 = roomJpaRepository.save(
                TestEntityFactory.createCustomRoom(book, category, LocalDate.now().minusDays(2), LocalDate.now().plusDays(10), RoomStatus.IN_PROGRESS)
        );
        // 방2: 시작일 오늘-1
        RoomJpaEntity room2 = roomJpaRepository.save(
                TestEntityFactory.createCustomRoom(book, category, LocalDate.now().minusDays(1), LocalDate.now().plusDays(8), RoomStatus.IN_PROGRESS)
        );
        // 방3: 시작일 오늘
        RoomJpaEntity room3 = roomJpaRepository.save(
                TestEntityFactory.createCustomRoom(book, category, LocalDate.now(), LocalDate.now().plusDays(9), RoomStatus.IN_PROGRESS)
        );

        // 모두 동일한 진행률(70%)로 참여
        roomParticipantJpaRepository.save(TestEntityFactory.createRoomParticipant(room1, newUser, RoomParticipantRole.MEMBER, 70.0));
        roomParticipantJpaRepository.save(TestEntityFactory.createRoomParticipant(room2, newUser, RoomParticipantRole.MEMBER, 70.0));
        roomParticipantJpaRepository.save(TestEntityFactory.createRoomParticipant(room3, newUser, RoomParticipantRole.MEMBER, 70.0));

        Long userId = newUser.getUserId();

        // when
        ResultActions result = mockMvc.perform(get("/rooms/home/joined")
                .requestAttr("userId", userId));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roomList", hasSize(3)))
                .andExpect(jsonPath("$.data.nextCursor").value(Matchers.nullValue()))
                .andExpect(jsonPath("$.data.isLast", is(true)))
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
        Alias alias = TestEntityFactory.createLiteratureAlias();
        UserJpaEntity newUser = userJpaRepository.save(TestEntityFactory.createUser(alias));

        // 모집중(시작일 미래)
        RoomJpaEntity recruitRoom = roomJpaRepository.save(
                TestEntityFactory.createCustomRoom(book, category, LocalDate.now().plusDays(2), LocalDate.now().plusDays(5), RoomStatus.RECRUITING)
        );
        // 활동중(시작일 오늘-1, 종료일 오늘+2)
        RoomJpaEntity activeRoom = roomJpaRepository.save(
                TestEntityFactory.createCustomRoom(book, category, LocalDate.now().minusDays(1), LocalDate.now().plusDays(2), RoomStatus.IN_PROGRESS)
        );

        roomParticipantJpaRepository.save(TestEntityFactory.createRoomParticipant(recruitRoom, newUser, RoomParticipantRole.MEMBER, 20.0));
        roomParticipantJpaRepository.save(TestEntityFactory.createRoomParticipant(activeRoom, newUser, RoomParticipantRole.MEMBER, 50.0));


        // when
        ResultActions result = mockMvc.perform(get("/rooms/home/joined")
                .requestAttr("userId", newUser.getUserId()));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roomList", hasSize(1)))
                .andExpect(jsonPath("$.data.nextCursor").value(Matchers.nullValue()))
                .andExpect(jsonPath("$.data.isLast", is(true)))
                .andExpect(jsonPath("$.data.roomList[0].roomId", is(activeRoom.getRoomId().intValue())))
                .andExpect(jsonPath("$.data.roomList[0].userPercentage", is(50)));
    }


    @Test
    @DisplayName("사용자가 참여중인 방이 없으면 빈 리스트를 반환한다.")
    void getHomeJoinedRooms_empty() throws Exception {

        //given
        Alias alias = TestEntityFactory.createLiteratureAlias();
        UserJpaEntity newUser = userJpaRepository.save(TestEntityFactory.createUser(alias));

        //when
        ResultActions result = mockMvc.perform(get("/rooms/home/joined")
                .requestAttr("userId", newUser.getUserId()));

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roomList", hasSize(0)))
                .andExpect(jsonPath("$.data.nextCursor").value(Matchers.nullValue()))
                .andExpect(jsonPath("$.data.isLast", is(true)));
    }

    @Test
    @DisplayName("한번에 최대 10개의 데이터만을 반환한다. 다음 페이지에 해당하는 데이터가 있을 경우, 다음 페이지의 cursor 값을 반환한다. 또한 cursor 값을 기준으로 해당 페이지의 데이터를 반환한다.")
    void getHomeJoinedRooms_page_1() throws Exception {

        //given
        UserJpaEntity user = userJpaRepository.save(TestEntityFactory.createUser(TestEntityFactory.createScienceAlias()));

        // 방 생성 + 멤버 카운트 + 유저 참여 및 진행률 세팅
        for (int i = 1; i < 13; i++) {
            String isbn = "isbn" + (i + 1);
            String title = "과학-방-" + i + "일전-활동시작";
            LocalDate start = LocalDate.now().minusDays(i);
            LocalDate end = LocalDate.now().plusDays(30);

            RoomJpaEntity room = saveScienceRoom("모집중인방-책-" + (i + 1), isbn, title, start, end, 10);
            room.updateRoomStatus(RoomStatus.IN_PROGRESS);
            changeRoomMemberCount(room, 8);

            double userPercentage = 89.6 - i; // 진행률은 방번호가 작을수록 높음

            saveSingleUserToRoom(room, user, userPercentage);
        }

        //when 첫 페이지 조회
        ResultActions firstPage = mockMvc.perform(get("/rooms/home/joined")
                .requestAttr("userId", user.getUserId()));

        //then
        firstPage.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isLast", is(false)))
                .andExpect(jsonPath("$.data.roomList", hasSize(10)))
                .andExpect(jsonPath("$.data.nextCursor").exists())
                // 정렬 조건 : 유저 진행도 순
                .andExpect(jsonPath("$.data.roomList[0].roomTitle", is("과학-방-1일전-활동시작")))
                .andExpect(jsonPath("$.data.roomList[1].roomTitle", is("과학-방-2일전-활동시작")))
                .andExpect(jsonPath("$.data.roomList[2].roomTitle", is("과학-방-3일전-활동시작")))
                .andExpect(jsonPath("$.data.roomList[3].roomTitle", is("과학-방-4일전-활동시작")))
                .andExpect(jsonPath("$.data.roomList[4].roomTitle", is("과학-방-5일전-활동시작")))
                .andExpect(jsonPath("$.data.roomList[5].roomTitle", is("과학-방-6일전-활동시작")))
                .andExpect(jsonPath("$.data.roomList[6].roomTitle", is("과학-방-7일전-활동시작")))
                .andExpect(jsonPath("$.data.roomList[7].roomTitle", is("과학-방-8일전-활동시작")))
                .andExpect(jsonPath("$.data.roomList[8].roomTitle", is("과학-방-9일전-활동시작")))
                .andExpect(jsonPath("$.data.roomList[9].roomTitle", is("과학-방-10일전-활동시작")));

        String responseBody = firstPage.andReturn().getResponse().getContentAsString();
        String nextCursor = com.jayway.jsonpath.JsonPath.read(responseBody, "$.data.nextCursor");

        //when 두번째 페이지 조회
        ResultActions secondPage = mockMvc.perform(get("/rooms/home/joined")
                .requestAttr("userId", user.getUserId())
                .param("size", "10")
                .param("cursor", nextCursor)
        );

        secondPage.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roomList", hasSize(2)))
                .andExpect(jsonPath("$.data.nextCursor").value(Matchers.nullValue()))
                .andExpect(jsonPath("$.data.isLast", is(true)))
                .andExpect(jsonPath("$.data.roomList[0].roomTitle", is("과학-방-11일전-활동시작")))
                .andExpect(jsonPath("$.data.roomList[1].roomTitle", is("과학-방-12일전-활동시작")));

    }


}
