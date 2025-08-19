package konkuk.thip.room.adapter.in.web;

import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.room.adapter.out.jpa.RoomJpaEntity;
import konkuk.thip.room.adapter.out.jpa.RoomParticipantJpaEntity;
import konkuk.thip.room.adapter.out.jpa.RoomParticipantRole;
import konkuk.thip.room.adapter.out.persistence.repository.RoomJpaRepository;
import konkuk.thip.room.adapter.out.persistence.repository.category.CategoryJpaRepository;
import konkuk.thip.room.adapter.out.persistence.repository.roomparticipant.RoomParticipantJpaRepository;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.user.adapter.out.persistence.repository.alias.AliasJpaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;

import static konkuk.thip.common.exception.code.ErrorCode.INVALID_MY_ROOM_TYPE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("[통합] 내 방 목록 조회 api 통합 테스트")
class RoomShowMineApiTest {

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
    private RoomParticipantJpaRepository roomParticipantJpaRepository;

    @AfterEach
    void tearDown() {
        roomParticipantJpaRepository.deleteAllInBatch();
        roomJpaRepository.deleteAllInBatch();
        bookJpaRepository.deleteAllInBatch();
        userJpaRepository.deleteAllInBatch();
        categoryJpaRepository.deleteAllInBatch();
        aliasJpaRepository.deleteAllInBatch();
    }

    private RoomJpaEntity saveScienceRoom(String bookTitle, String isbn, String roomName, LocalDate startDate, LocalDate endDate, int recruitCount) {
        AliasJpaEntity alias = aliasJpaRepository.save(TestEntityFactory.createScienceAlias());

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

        CategoryJpaEntity category = categoryJpaRepository.save(TestEntityFactory.createScienceCategory(alias));

        return roomJpaRepository.save(RoomJpaEntity.builder()
                .title(roomName)
                .description("한강 작품 읽기 모임")
                .isPublic(true)
                .roomPercentage(0.0)
                .startDate(startDate)
                .endDate(endDate)
                .recruitCount(recruitCount)
                .bookJpaEntity(book)
                .categoryJpaEntity(category)
                .build());
    }

    private void changeRoomMemberCount(RoomJpaEntity roomJpaEntity, int count) {
        roomJpaEntity.updateMemberCount(count);
        roomJpaRepository.save(roomJpaEntity);
    }

    private void saveSingleUserToRoom(RoomJpaEntity roomJpaEntity, UserJpaEntity userJpaEntity) {
        RoomParticipantJpaEntity roomParticipantJpaEntity = RoomParticipantJpaEntity.builder()
                .userJpaEntity(userJpaEntity)
                .roomJpaEntity(roomJpaEntity)
                .roomParticipantRole(RoomParticipantRole.MEMBER)
                .build();
        roomParticipantJpaRepository.save(roomParticipantJpaEntity);

        roomJpaEntity.updateMemberCount(roomJpaEntity.getMemberCount() + 1);
        roomJpaRepository.save(roomJpaEntity);      // room의 memberCount 값도 업데이트 해줘야 한다
    }

    @Test
    @DisplayName("type 으로 playing 을 받을 경우, 해당 유저가 참여중인 방 중 [현재 진행중인 모임방]의 정보를 [활동 마감일 임박순] 으로 반환한다.")
    void get_my_playing_rooms() throws Exception {
        //given
        RoomJpaEntity recruitingRoom1 = saveScienceRoom("모집중인방-책-1", "isbn1", "과학-방-1일뒤-활동시작", LocalDate.now().plusDays(1), LocalDate.now().plusDays(30), 10);
        changeRoomMemberCount(recruitingRoom1, 5);

        RoomJpaEntity playingRoom1 = saveScienceRoom("진행중인방-책-1", "isbn2", "과학-방-5일뒤-활동마감", LocalDate.now().minusDays(5), LocalDate.now().plusDays(5), 10);
        changeRoomMemberCount(playingRoom1, 6);

        RoomJpaEntity playingRoom2 = saveScienceRoom("진행중인방-책-2", "isbn3", "과학-방-10일뒤-활동마감", LocalDate.now().minusDays(5), LocalDate.now().plusDays(10), 10);
        changeRoomMemberCount(playingRoom2, 3);

        RoomJpaEntity expiredRoom1 = saveScienceRoom("만료된방-책-1", "isbn4", "과학-방-5일전-활동마감", LocalDate.now().minusDays(30), LocalDate.now().minusDays(5), 10);
        changeRoomMemberCount(expiredRoom1, 7);

        AliasJpaEntity scienceAlias = aliasJpaRepository.save(TestEntityFactory.createScienceAlias());
        UserJpaEntity user = userJpaRepository.save(TestEntityFactory.createUser(scienceAlias));

        // user가 생성한 방에 참여한 상황 가정
        saveSingleUserToRoom(recruitingRoom1, user);
        saveSingleUserToRoom(playingRoom1, user);
        saveSingleUserToRoom(playingRoom2, user);
        saveSingleUserToRoom(expiredRoom1, user);

        //when
        ResultActions result = mockMvc.perform(get("/rooms/my")
                .requestAttr("userId", user.getUserId())
                .param("type", "playing"));     // 진행중인 방

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roomList", hasSize(2)))
                .andExpect(jsonPath("$.data.roomList[0].roomName", is("과학-방-5일뒤-활동마감")))
                .andExpect(jsonPath("$.data.roomList[0].memberCount", is(7)))      // 기존 6명 + user
                .andExpect(jsonPath("$.data.roomList[1].roomName", is("과학-방-10일뒤-활동마감")))
                .andExpect(jsonPath("$.data.roomList[1].memberCount", is(4)));      // 기존 3명 + user
    }

    @Test
    @DisplayName("type 으로 recruiting 을 받을 경우, 해당 유저가 참여중인 방 중 [모집중인 모임방]의 정보를 [모집 마감일 임박순] 으로 반환한다.")
    void get_my_recruiting_rooms() throws Exception {
        //given
        RoomJpaEntity recruitingRoom1 = saveScienceRoom("모집중인방-책-1", "isbn1", "과학-방-1일뒤-활동시작", LocalDate.now().plusDays(1), LocalDate.now().plusDays(30), 10);
        changeRoomMemberCount(recruitingRoom1, 5);

        RoomJpaEntity recruitingRoom2 = saveScienceRoom("모집중인방-책-2", "isbn2", "과학-방-5일뒤-활동시작", LocalDate.now().plusDays(5), LocalDate.now().plusDays(30), 10);
        changeRoomMemberCount(recruitingRoom2, 8);

        RoomJpaEntity playingRoom1 = saveScienceRoom("진행중인방-책-1", "isbn3", "과학-방-5일뒤-활동마감", LocalDate.now().minusDays(5), LocalDate.now().plusDays(5), 10);
        changeRoomMemberCount(playingRoom1, 6);

        RoomJpaEntity expiredRoom1 = saveScienceRoom("만료된방-책-1", "isbn4", "과학-방-5일전-활동마감", LocalDate.now().minusDays(30), LocalDate.now().minusDays(5), 10);
        changeRoomMemberCount(expiredRoom1, 7);

        AliasJpaEntity scienceAlias = aliasJpaRepository.save(TestEntityFactory.createScienceAlias());
        UserJpaEntity user = userJpaRepository.save(TestEntityFactory.createUser(scienceAlias));

        // user가 생성한 방에 참여한 상황 가정
        saveSingleUserToRoom(recruitingRoom1, user);
        saveSingleUserToRoom(recruitingRoom2, user);
        saveSingleUserToRoom(playingRoom1, user);
        saveSingleUserToRoom(expiredRoom1, user);

        //when
        ResultActions result = mockMvc.perform(get("/rooms/my")
                .requestAttr("userId", user.getUserId())
                .param("type", "recruiting"));      // 모집중인 방

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roomList", hasSize(2)))
                .andExpect(jsonPath("$.data.roomList[0].roomName", is("과학-방-1일뒤-활동시작")))
                .andExpect(jsonPath("$.data.roomList[0].memberCount", is(6)))       // 기존 5명 + user
                .andExpect(jsonPath("$.data.roomList[1].roomName", is("과학-방-5일뒤-활동시작")))
                .andExpect(jsonPath("$.data.roomList[1].memberCount", is(9)));      // 기존 8명 + user
    }

    @Test
    @DisplayName("type 이 주어지지 않을 경우, 해당 유저가 참여중인 방 중 [현재 진행중 + 모집중인 모임방]의 정보를 [현재 진행중 -> 모집중] 순 으로 반환한다.")
    void get_my_playing_and_recruiting_rooms() throws Exception {
        //given
        RoomJpaEntity recruitingRoom1 = saveScienceRoom("모집중인방-책-1", "isbn1", "과학-방-1일뒤-활동시작", LocalDate.now().plusDays(1), LocalDate.now().plusDays(30), 10);
        changeRoomMemberCount(recruitingRoom1, 5);

        RoomJpaEntity recruitingRoom2 = saveScienceRoom("모집중인방-책-2", "isbn2", "과학-방-5일뒤-활동시작", LocalDate.now().plusDays(5), LocalDate.now().plusDays(30), 10);
        changeRoomMemberCount(recruitingRoom2, 8);

        RoomJpaEntity playingRoom1 = saveScienceRoom("진행중인방-책-1", "isbn3", "과학-방-5일뒤-활동마감", LocalDate.now().minusDays(5), LocalDate.now().plusDays(5), 10);
        changeRoomMemberCount(playingRoom1, 6);

        RoomJpaEntity expiredRoom1 = saveScienceRoom("만료된방-책-1", "isbn4", "과학-방-5일전-활동마감", LocalDate.now().minusDays(30), LocalDate.now().minusDays(5), 10);
        changeRoomMemberCount(expiredRoom1, 7);

        AliasJpaEntity scienceAlias = aliasJpaRepository.save(TestEntityFactory.createScienceAlias());
        UserJpaEntity user = userJpaRepository.save(TestEntityFactory.createUser(scienceAlias));

        // user가 생성한 방에 참여한 상황 가정
        saveSingleUserToRoom(recruitingRoom1, user);
        saveSingleUserToRoom(recruitingRoom2, user);
        saveSingleUserToRoom(playingRoom1, user);
        saveSingleUserToRoom(expiredRoom1, user);

        //when
        ResultActions result = mockMvc.perform(get("/rooms/my")
                .requestAttr("userId", user.getUserId()));      // type request param 없는 경우

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roomList", hasSize(3)))
                .andExpect(jsonPath("$.data.roomList[0].roomName", is("과학-방-5일뒤-활동마감")))
                .andExpect(jsonPath("$.data.roomList[0].memberCount", is(7)))       // 기존 6명 + user
                .andExpect(jsonPath("$.data.roomList[1].roomName", is("과학-방-1일뒤-활동시작")))
                .andExpect(jsonPath("$.data.roomList[1].memberCount", is(6)))       // 기존 5명 + user
                .andExpect(jsonPath("$.data.roomList[2].roomName", is("과학-방-5일뒤-활동시작")))
                .andExpect(jsonPath("$.data.roomList[2].memberCount", is(9)));       // 기존 8명 + user
    }

    @Test
    @DisplayName("type 으로 expired 을 받을 경우, 해당 유저가 참여중인 방 중 [만료된 모임방]의 정보를 [활동 마감일 최신순] 으로 반환한다.")
    void get_my_expired_rooms() throws Exception {
        //given
        RoomJpaEntity recruitingRoom1 = saveScienceRoom("모집중인방-책-1", "isbn1", "과학-방-1일뒤-활동시작", LocalDate.now().plusDays(1), LocalDate.now().plusDays(30), 10);
        changeRoomMemberCount(recruitingRoom1, 5);

        RoomJpaEntity playingRoom1 = saveScienceRoom("진행중인방-책-1", "isbn2", "과학-방-5일뒤-활동마감", LocalDate.now().minusDays(5), LocalDate.now().plusDays(5), 10);
        changeRoomMemberCount(playingRoom1, 6);

        RoomJpaEntity expiredRoom1 = saveScienceRoom("만료된방-책-1", "isbn3", "과학-방-5일전-활동마감", LocalDate.now().minusDays(30), LocalDate.now().minusDays(5), 10);
        changeRoomMemberCount(expiredRoom1, 7);

        RoomJpaEntity expiredRoom2 = saveScienceRoom("만료된방-책-2", "isbn4", "과학-방-10일전-활동마감", LocalDate.now().minusDays(30), LocalDate.now().minusDays(10), 10);
        changeRoomMemberCount(expiredRoom2, 1);

        AliasJpaEntity scienceAlias = aliasJpaRepository.save(TestEntityFactory.createScienceAlias());
        UserJpaEntity user = userJpaRepository.save(TestEntityFactory.createUser(scienceAlias));

        // user가 생성한 방에 참여한 상황 가정
        saveSingleUserToRoom(recruitingRoom1, user);
        saveSingleUserToRoom(playingRoom1, user);
        saveSingleUserToRoom(expiredRoom1, user);
        saveSingleUserToRoom(expiredRoom2, user);

        //when
        ResultActions result = mockMvc.perform(get("/rooms/my")
                .requestAttr("userId", user.getUserId())
                .param("type", "expired"));     // 만료된 방

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roomList", hasSize(2)))
                .andExpect(jsonPath("$.data.roomList[0].roomName", is("과학-방-5일전-활동마감")))
                .andExpect(jsonPath("$.data.roomList[0].memberCount", is(8)))      // 기존 7명 + user
                .andExpect(jsonPath("$.data.roomList[1].roomName", is("과학-방-10일전-활동마감")))
                .andExpect(jsonPath("$.data.roomList[1].memberCount", is(2)));      // 기존 1명 + user
    }

    @Test
    @DisplayName("유효하지 않은 type 을 받을 경우, 400 error 를 반환한다.")
    void get_my_rooms_wrong_type() throws Exception {
        //given
        RoomJpaEntity recruitingRoom1 = saveScienceRoom("모집중인방-책-1", "isbn1", "과학-방-1일뒤-활동시작", LocalDate.now().plusDays(1), LocalDate.now().plusDays(30), 10);
        changeRoomMemberCount(recruitingRoom1, 5);

        RoomJpaEntity recruitingRoom2 = saveScienceRoom("모집중인방-책-2", "isbn2", "과학-방-5일뒤-활동시작", LocalDate.now().plusDays(5), LocalDate.now().plusDays(30), 10);
        changeRoomMemberCount(recruitingRoom2, 8);

        RoomJpaEntity playingRoom1 = saveScienceRoom("진행중인방-책-1", "isbn3", "과학-방-5일뒤-활동마감", LocalDate.now().minusDays(5), LocalDate.now().plusDays(5), 10);
        changeRoomMemberCount(playingRoom1, 6);

        RoomJpaEntity expiredRoom1 = saveScienceRoom("만료된방-책-1", "isbn4", "과학-방-5일전-활동마감", LocalDate.now().minusDays(30), LocalDate.now().minusDays(5), 10);
        changeRoomMemberCount(expiredRoom1, 7);

        AliasJpaEntity scienceAlias = aliasJpaRepository.save(TestEntityFactory.createScienceAlias());
        UserJpaEntity user = userJpaRepository.save(TestEntityFactory.createUser(scienceAlias));

        // user가 생성한 방에 참여한 상황 가정
        saveSingleUserToRoom(recruitingRoom1, user);
        saveSingleUserToRoom(recruitingRoom2, user);
        saveSingleUserToRoom(playingRoom1, user);
        saveSingleUserToRoom(expiredRoom1, user);

        //when
        ResultActions result = mockMvc.perform(get("/rooms/my")
                .requestAttr("userId", user.getUserId())
                .param("type", "wrongType"));     // 이상한 type request param

        //then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess", is(false)))
                .andExpect(jsonPath("$.code", is(INVALID_MY_ROOM_TYPE.getCode())));
    }

    @Test
    @DisplayName("한번에 최대 10개의 데이터만을 반환한다. 다음 페이지에 해당하는 데이터가 있을 경우, 다음 페이지의 cursor 값을 반환한다.")
    void get_my_rooms_page_1() throws Exception {
        //given
        RoomJpaEntity recruitingRoom1 = saveScienceRoom("모집중인방-책-1", "isbn1", "과학-방-1일뒤-활동시작", LocalDate.now().plusDays(1), LocalDate.now().plusDays(30), 10);
        changeRoomMemberCount(recruitingRoom1, 5);

        RoomJpaEntity recruitingRoom2 = saveScienceRoom("모집중인방-책-2", "isbn2", "과학-방-2일뒤-활동시작", LocalDate.now().plusDays(2), LocalDate.now().plusDays(30), 10);
        changeRoomMemberCount(recruitingRoom2, 8);

        RoomJpaEntity recruitingRoom3 = saveScienceRoom("모집중인방-책-3", "isbn3", "과학-방-3일뒤-활동시작", LocalDate.now().plusDays(3), LocalDate.now().plusDays(30), 10);
        changeRoomMemberCount(recruitingRoom3, 8);

        RoomJpaEntity recruitingRoom4 = saveScienceRoom("모집중인방-책-4", "isbn4", "과학-방-4일뒤-활동시작", LocalDate.now().plusDays(4), LocalDate.now().plusDays(30), 10);
        changeRoomMemberCount(recruitingRoom4, 8);

        RoomJpaEntity recruitingRoom5 = saveScienceRoom("모집중인방-책-5", "isbn5", "과학-방-5일뒤-활동시작", LocalDate.now().plusDays(5), LocalDate.now().plusDays(30), 10);
        changeRoomMemberCount(recruitingRoom5, 8);

        RoomJpaEntity recruitingRoom6 = saveScienceRoom("모집중인방-책-6", "isbn6", "과학-방-6일뒤-활동시작", LocalDate.now().plusDays(6), LocalDate.now().plusDays(30), 10);
        changeRoomMemberCount(recruitingRoom6, 8);

        RoomJpaEntity recruitingRoom7 = saveScienceRoom("모집중인방-책-7", "isbn7", "과학-방-7일뒤-활동시작", LocalDate.now().plusDays(7), LocalDate.now().plusDays(30), 10);
        changeRoomMemberCount(recruitingRoom7, 8);

        RoomJpaEntity recruitingRoom8 = saveScienceRoom("모집중인방-책-8", "isbn8", "과학-방-8일뒤-활동시작", LocalDate.now().plusDays(8), LocalDate.now().plusDays(30), 10);
        changeRoomMemberCount(recruitingRoom8, 8);

        RoomJpaEntity recruitingRoom9 = saveScienceRoom("모집중인방-책-9", "isbn9", "과학-방-9일뒤-활동시작", LocalDate.now().plusDays(9), LocalDate.now().plusDays(30), 10);
        changeRoomMemberCount(recruitingRoom9, 8);

        RoomJpaEntity recruitingRoom10 = saveScienceRoom("모집중인방-책-10", "isbn10", "과학-방-10일뒤-활동시작", LocalDate.now().plusDays(10), LocalDate.now().plusDays(30), 10);
        changeRoomMemberCount(recruitingRoom10, 8);

        RoomJpaEntity recruitingRoom11 = saveScienceRoom("모집중인방-책-11", "isbn11", "과학-방-11일뒤-활동시작", LocalDate.now().plusDays(11), LocalDate.now().plusDays(30), 10);
        changeRoomMemberCount(recruitingRoom11, 8);

        RoomJpaEntity recruitingRoom12 = saveScienceRoom("모집중인방-책-12", "isbn12", "과학-방-12일뒤-활동시작", LocalDate.now().plusDays(12), LocalDate.now().plusDays(30), 10);
        changeRoomMemberCount(recruitingRoom12, 8);

        AliasJpaEntity scienceAlias = aliasJpaRepository.save(TestEntityFactory.createScienceAlias());
        UserJpaEntity user = userJpaRepository.save(TestEntityFactory.createUser(scienceAlias));

        // user가 생성한 방에 참여한 상황 가정
        saveSingleUserToRoom(recruitingRoom1, user);
        saveSingleUserToRoom(recruitingRoom2, user);
        saveSingleUserToRoom(recruitingRoom3, user);
        saveSingleUserToRoom(recruitingRoom4, user);
        saveSingleUserToRoom(recruitingRoom5, user);
        saveSingleUserToRoom(recruitingRoom6, user);
        saveSingleUserToRoom(recruitingRoom7, user);
        saveSingleUserToRoom(recruitingRoom8, user);
        saveSingleUserToRoom(recruitingRoom9, user);
        saveSingleUserToRoom(recruitingRoom10, user);
        saveSingleUserToRoom(recruitingRoom11, user);
        saveSingleUserToRoom(recruitingRoom12, user);

        //when
        ResultActions result = mockMvc.perform(get("/rooms/my")
                .requestAttr("userId", user.getUserId())
                .param("type", "recruiting"));

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isLast", is(false)))
                .andExpect(jsonPath("$.data.roomList", hasSize(10)))
                // 정렬 조건 : 모집중인 방 == 방 활동 시작일 임박 순
                .andExpect(jsonPath("$.data.roomList[0].roomName", is("과학-방-1일뒤-활동시작")))
                .andExpect(jsonPath("$.data.roomList[1].roomName", is("과학-방-2일뒤-활동시작")))
                .andExpect(jsonPath("$.data.roomList[2].roomName", is("과학-방-3일뒤-활동시작")))
                .andExpect(jsonPath("$.data.roomList[3].roomName", is("과학-방-4일뒤-활동시작")))
                .andExpect(jsonPath("$.data.roomList[4].roomName", is("과학-방-5일뒤-활동시작")))
                .andExpect(jsonPath("$.data.roomList[5].roomName", is("과학-방-6일뒤-활동시작")))
                .andExpect(jsonPath("$.data.roomList[6].roomName", is("과학-방-7일뒤-활동시작")))
                .andExpect(jsonPath("$.data.roomList[7].roomName", is("과학-방-8일뒤-활동시작")))
                .andExpect(jsonPath("$.data.roomList[8].roomName", is("과학-방-9일뒤-활동시작")))
                .andExpect(jsonPath("$.data.roomList[9].roomName", is("과학-방-10일뒤-활동시작")));
    }

    @Test
    @DisplayName("cursor 값을 기준으로 해당 페이지의 데이터를 반환한다.")
    void get_my_rooms_page_2() throws Exception {
        //given
        RoomJpaEntity recruitingRoom1 = saveScienceRoom("모집중인방-책-1", "isbn1", "과학-방-1일뒤-활동시작", LocalDate.now().plusDays(1), LocalDate.now().plusDays(30), 10);
        changeRoomMemberCount(recruitingRoom1, 5);

        RoomJpaEntity recruitingRoom2 = saveScienceRoom("모집중인방-책-2", "isbn2", "과학-방-2일뒤-활동시작", LocalDate.now().plusDays(2), LocalDate.now().plusDays(30), 10);
        changeRoomMemberCount(recruitingRoom2, 8);

        RoomJpaEntity recruitingRoom3 = saveScienceRoom("모집중인방-책-3", "isbn3", "과학-방-3일뒤-활동시작", LocalDate.now().plusDays(3), LocalDate.now().plusDays(30), 10);
        changeRoomMemberCount(recruitingRoom3, 8);

        RoomJpaEntity recruitingRoom4 = saveScienceRoom("모집중인방-책-4", "isbn4", "과학-방-4일뒤-활동시작", LocalDate.now().plusDays(4), LocalDate.now().plusDays(30), 10);
        changeRoomMemberCount(recruitingRoom4, 8);

        RoomJpaEntity recruitingRoom5 = saveScienceRoom("모집중인방-책-5", "isbn5", "과학-방-5일뒤-활동시작", LocalDate.now().plusDays(5), LocalDate.now().plusDays(30), 10);
        changeRoomMemberCount(recruitingRoom5, 8);

        RoomJpaEntity recruitingRoom6 = saveScienceRoom("모집중인방-책-6", "isbn6", "과학-방-6일뒤-활동시작", LocalDate.now().plusDays(6), LocalDate.now().plusDays(30), 10);
        changeRoomMemberCount(recruitingRoom6, 8);

        RoomJpaEntity recruitingRoom7 = saveScienceRoom("모집중인방-책-7", "isbn7", "과학-방-7일뒤-활동시작", LocalDate.now().plusDays(7), LocalDate.now().plusDays(30), 10);
        changeRoomMemberCount(recruitingRoom7, 8);

        RoomJpaEntity recruitingRoom8 = saveScienceRoom("모집중인방-책-8", "isbn8", "과학-방-8일뒤-활동시작", LocalDate.now().plusDays(8), LocalDate.now().plusDays(30), 10);
        changeRoomMemberCount(recruitingRoom8, 8);

        RoomJpaEntity recruitingRoom9 = saveScienceRoom("모집중인방-책-9", "isbn9", "과학-방-9일뒤-활동시작", LocalDate.now().plusDays(9), LocalDate.now().plusDays(30), 10);
        changeRoomMemberCount(recruitingRoom9, 8);

        RoomJpaEntity recruitingRoom10 = saveScienceRoom("모집중인방-책-10", "isbn10", "과학-방-10일뒤-활동시작", LocalDate.now().plusDays(10), LocalDate.now().plusDays(30), 10);
        changeRoomMemberCount(recruitingRoom10, 8);

        RoomJpaEntity recruitingRoom11 = saveScienceRoom("모집중인방-책-11", "isbn11", "과학-방-11일뒤-활동시작", LocalDate.now().plusDays(11), LocalDate.now().plusDays(30), 10);
        changeRoomMemberCount(recruitingRoom11, 8);

        RoomJpaEntity recruitingRoom12 = saveScienceRoom("모집중인방-책-12", "isbn12", "과학-방-12일뒤-활동시작", LocalDate.now().plusDays(12), LocalDate.now().plusDays(30), 10);
        changeRoomMemberCount(recruitingRoom12, 8);

        AliasJpaEntity scienceAlias = aliasJpaRepository.save(TestEntityFactory.createScienceAlias());
        UserJpaEntity user = userJpaRepository.save(TestEntityFactory.createUser(scienceAlias));

        // user가 생성한 방에 참여한 상황 가정
        saveSingleUserToRoom(recruitingRoom1, user);
        saveSingleUserToRoom(recruitingRoom2, user);
        saveSingleUserToRoom(recruitingRoom3, user);
        saveSingleUserToRoom(recruitingRoom4, user);
        saveSingleUserToRoom(recruitingRoom5, user);
        saveSingleUserToRoom(recruitingRoom6, user);
        saveSingleUserToRoom(recruitingRoom7, user);
        saveSingleUserToRoom(recruitingRoom8, user);
        saveSingleUserToRoom(recruitingRoom9, user);
        saveSingleUserToRoom(recruitingRoom10, user);
        saveSingleUserToRoom(recruitingRoom11, user);
        saveSingleUserToRoom(recruitingRoom12, user);

        //when
        String nextCursor = recruitingRoom10.getStartDate().toString() + "|" + recruitingRoom10.getRoomId().toString();     // 이전 페이지의 마지막 레코드인 room10이 nextCursor이다

        ResultActions result = mockMvc.perform(get("/rooms/my")
                .requestAttr("userId", user.getUserId())
                .param("type", "recruiting")
                .param("cursor", nextCursor));

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isLast", is(true)))
                .andExpect(jsonPath("$.data.roomList", hasSize(2)))
                .andExpect(jsonPath("$.data.roomList[0].roomName", is("과학-방-11일뒤-활동시작")))
                .andExpect(jsonPath("$.data.roomList[1].roomName", is("과학-방-12일뒤-활동시작")));
    }
}
