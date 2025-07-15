package konkuk.thip.room.adapter.in.web;

import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.common.util.DateUtil;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("[통합] 모집 중인 방 상세조회 api 통합 테스트")
class RoomRecruitingDetailViewApiTest {

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

    @AfterEach
    void tearDown() {
        userRoomJpaRepository.deleteAll();
        roomJpaRepository.deleteAll();
        bookJpaRepository.deleteAll();
        userJpaRepository.deleteAll();
        categoryJpaRepository.deleteAll();
        aliasJpaRepository.deleteAll();
    }

    private RoomJpaEntity saveScienceRoom(String bookTitle, String isbn, String roomName, LocalDate startDate, int recruitCount) {
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
                .endDate(LocalDate.now().plusDays(30))
                .recruitCount(recruitCount)
                .bookJpaEntity(book)
                .categoryJpaEntity(category)
                .build());
    }

    private RoomJpaEntity saveLiteratureRoom(String bookTitle, String isbn, String roomName, LocalDate startDate, int recruitCount) {
        AliasJpaEntity alias = aliasJpaRepository.save(TestEntityFactory.createLiteratureAlias());

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

        CategoryJpaEntity category = categoryJpaRepository.save(TestEntityFactory.createLiteratureCategory(alias));

        return roomJpaRepository.save(RoomJpaEntity.builder()
                .title(roomName)
                .description("한강 작품 읽기 모임")
                .isPublic(true)
                .roomPercentage(0.0)
                .startDate(startDate)
                .endDate(LocalDate.now().plusDays(30))
                .recruitCount(recruitCount)
                .bookJpaEntity(book)
                .categoryJpaEntity(category)
                .build());
    }

    private void saveUsersToRoom(RoomJpaEntity roomJpaEntity, int count) {
        AliasJpaEntity alias = aliasJpaRepository.save(TestEntityFactory.createScienceAlias());

        // User 리스트 생성 및 저장
        List<UserJpaEntity> users = IntStream.rangeClosed(1, count)
                .mapToObj(i -> UserJpaEntity.builder()
                        .nickname("user" + i)
                        .imageUrl("http://image")
                        .oauth2Id("oauth2Id")
                        .role(UserRole.USER)
                        .aliasForUserJpaEntity(alias)
                        .build())
                .toList();

        List<UserJpaEntity> savedUsers = userJpaRepository.saveAll(users);

        // UserRoom 매핑 리스트 생성 및 저장
        List<UserRoomJpaEntity> mappings = savedUsers.stream()
                .map(user -> UserRoomJpaEntity.builder()
                        .userJpaEntity(user)
                        .roomJpaEntity(roomJpaEntity)
                        .userRoomRole(UserRoomRole.MEMBER)
                        .build())
                .toList();

        userRoomJpaRepository.saveAll(mappings);
    }

    @Test
    @DisplayName("모집중인 모임방 상세조회할 경우, 해당 모임방의 정보, 책 정보, 추천할 모임방의 정보를 반환한다.")
    void get_recruiting_room_detail() throws Exception {
        //given
        RoomJpaEntity targetRoom = saveScienceRoom("과학-책", "isbn1", "과학-방-1일뒤-활동시작", LocalDate.now().plusDays(1), 10);
        saveUsersToRoom(targetRoom, 4);
        UserJpaEntity joiningUser = userRoomJpaRepository.findAllByRoomJpaEntity_RoomId(targetRoom.getRoomId()).get(1).getUserJpaEntity();

        RoomJpaEntity science_room_2 = saveScienceRoom("과학-책", "isbn2", "방이름입니다", LocalDate.now().plusDays(1), 10);
        saveUsersToRoom(science_room_2, 5);

        RoomJpaEntity science_room_3 = saveScienceRoom("과학-책", "isbn3", "무슨방일까요??", LocalDate.now().plusDays(5), 8);
        saveUsersToRoom(science_room_3, 2);

        RoomJpaEntity science_room_4 = saveScienceRoom("과학-책", "isbn4", "과학-방-8일뒤-활동시작", LocalDate.now().plusDays(8), 8);
        saveUsersToRoom(science_room_4, 1);

        RoomJpaEntity room_3 = saveLiteratureRoom("문학-책", "isbn5", "방제목에-과학-포함된-문학방", LocalDate.now().plusDays(10), 8);
        saveUsersToRoom(room_3, 6);

        RoomJpaEntity recruit_expired_room_4 = saveScienceRoom("과학-책", "isbn6", "모집기한-지난-과학방", LocalDate.now().minusDays(1), 8);
        saveUsersToRoom(recruit_expired_room_4, 6);

        //when
        ResultActions result = mockMvc.perform(get("/rooms/{roomId}/recruiting", targetRoom.getRoomId())
                .requestAttr("userId", joiningUser.getUserId()));

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isHost", is(false)))
                .andExpect(jsonPath("$.data.isJoining", is(true)))
                .andExpect(jsonPath("$.data.roomName", is("과학-방-1일뒤-활동시작")))
                .andExpect(jsonPath("$.data.roomImageUrl", is("과학/IT_image")))      // 방 대표 이미지 추가
                .andExpect(jsonPath("$.data.progressStartDate", is(DateUtil.formatDate(LocalDate.now().plusDays(1)))))
                .andExpect(jsonPath("$.data.memberCount", is(4)))
                .andExpect(jsonPath("$.data.recruitCount", is(10)))
                .andExpect(jsonPath("$.data.isbn", is("isbn1")))
                .andExpect(jsonPath("$.data.bookTitle", is("과학-책")))
                .andExpect(jsonPath("$.data.recommendRooms", hasSize(3)))
                /**
                 * recommendRooms 검증 : 현재 조회하는 방과 동일한 카테고리의 다른 방을 추천
                 * <정렬 순서> : 모집 마감 임박 순
                 */
                .andExpect(jsonPath("$.data.recommendRooms[0].roomName", is("방이름입니다")))
                .andExpect(jsonPath("$.data.recommendRooms[1].roomName", is("무슨방일까요??")))
                .andExpect(jsonPath("$.data.recommendRooms[2].roomName", is("과학-방-8일뒤-활동시작")));
    }

    @Test
    @DisplayName("모임방의 호스트가 조회할 경우, 유저가 해당 방의 호스트임을 응답값으로 보여준다. (나머지 응답값은 동일)")
    void get_recruiting_room_detail_host() throws Exception {
        //given
        RoomJpaEntity targetRoom = saveScienceRoom("과학-책", "isbn1", "과학-방-1일뒤-활동시작", LocalDate.now().plusDays(1), 10);
        saveUsersToRoom(targetRoom, 4);
        UserRoomJpaEntity firstMember = userRoomJpaRepository.findAllByRoomJpaEntity_RoomId(targetRoom.getRoomId()).get(1);
        userRoomJpaRepository.delete(firstMember);
        UserRoomJpaEntity roomCreator = userRoomJpaRepository.save(UserRoomJpaEntity.builder()
                .userJpaEntity(firstMember.getUserJpaEntity())
                .roomJpaEntity(firstMember.getRoomJpaEntity())
                .userRoomRole(UserRoomRole.HOST)
                .build());      // firstMember 을 MEMBER -> HOST 로 수정

        RoomJpaEntity science_room_2 = saveScienceRoom("과학-책", "isbn2", "방이름입니다", LocalDate.now().plusDays(1), 10);
        saveUsersToRoom(science_room_2, 5);

        RoomJpaEntity science_room_3 = saveScienceRoom("과학-책", "isbn3", "무슨방일까요??", LocalDate.now().plusDays(5), 8);
        saveUsersToRoom(science_room_3, 2);

        RoomJpaEntity science_room_4 = saveScienceRoom("과학-책", "isbn4", "과학-방-8일뒤-활동시작", LocalDate.now().plusDays(8), 8);
        saveUsersToRoom(science_room_4, 1);

        RoomJpaEntity room_3 = saveLiteratureRoom("문학-책", "isbn5", "방제목에-과학-포함된-문학방", LocalDate.now().plusDays(10), 8);
        saveUsersToRoom(room_3, 6);

        RoomJpaEntity recruit_expired_room_4 = saveScienceRoom("과학-책", "isbn6", "모집기한-지난-과학방", LocalDate.now().minusDays(1), 8);
        saveUsersToRoom(recruit_expired_room_4, 6);

        //when
        ResultActions result = mockMvc.perform(get("/rooms/{roomId}/recruiting", targetRoom.getRoomId())
                .requestAttr("userId", roomCreator.getUserJpaEntity().getUserId()));

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isHost", is(true)))
                .andExpect(jsonPath("$.data.isJoining", is(true)))
                .andExpect(jsonPath("$.data.roomName", is("과학-방-1일뒤-활동시작")))
                .andExpect(jsonPath("$.data.progressStartDate", is(DateUtil.formatDate(LocalDate.now().plusDays(1)))))
                .andExpect(jsonPath("$.data.memberCount", is(4)))
                .andExpect(jsonPath("$.data.recruitCount", is(10)))
                .andExpect(jsonPath("$.data.isbn", is("isbn1")))
                .andExpect(jsonPath("$.data.bookTitle", is("과학-책")))
                .andExpect(jsonPath("$.data.recommendRooms", hasSize(3)))
                /**
                 * recommendRooms 검증 : 현재 조회하는 방과 동일한 카테고리의 다른 방을 추천
                 * <정렬 순서> : 모집 마감 임박 순
                 */
                .andExpect(jsonPath("$.data.recommendRooms[0].roomName", is("방이름입니다")))
                .andExpect(jsonPath("$.data.recommendRooms[1].roomName", is("무슨방일까요??")))
                .andExpect(jsonPath("$.data.recommendRooms[2].roomName", is("과학-방-8일뒤-활동시작")));
    }

    @Test
    @DisplayName("추천하는 다른 모집중인 모임방이 많을 경우, 모집 기한 마감임박 순으로 최대 5개만 반환한다.")
    void get_recruiting_room_detail_too_many_recommend_rooms() throws Exception {
        //given
        RoomJpaEntity targetRoom = saveScienceRoom("과학-책", "isbn1", "과학-방-1일뒤-활동시작", LocalDate.now().plusDays(1), 10);
        saveUsersToRoom(targetRoom, 4);
        UserJpaEntity joiningUser = userRoomJpaRepository.findAllByRoomJpaEntity_RoomId(targetRoom.getRoomId()).get(1).getUserJpaEntity();

        RoomJpaEntity science_room_2 = saveScienceRoom("과학-책", "isbn2", "방이름입니다", LocalDate.now().plusDays(1), 10);
        saveUsersToRoom(science_room_2, 5);

        RoomJpaEntity science_room_3 = saveScienceRoom("과학-책", "isbn3", "무슨방일까요??", LocalDate.now().plusDays(5), 8);
        saveUsersToRoom(science_room_3, 2);

        RoomJpaEntity science_room_4 = saveScienceRoom("과학-책", "isbn4", "과학-방-8일뒤-활동시작", LocalDate.now().plusDays(8), 8);
        saveUsersToRoom(science_room_4, 1);

        RoomJpaEntity science_room_5 = saveScienceRoom("과학-책", "isbn5", "과학-방-10일뒤-활동시작", LocalDate.now().plusDays(10), 8);
        saveUsersToRoom(science_room_5, 1);

        RoomJpaEntity science_room_6 = saveScienceRoom("과학-책", "isbn6", "과학-방-15일뒤-활동시작", LocalDate.now().plusDays(15), 8);
        saveUsersToRoom(science_room_6, 1);

        RoomJpaEntity science_room_7 = saveScienceRoom("과학-책", "isbn7", "과학-방-20일뒤-활동시작", LocalDate.now().plusDays(20), 8);
        saveUsersToRoom(science_room_7, 1);

        //when
        ResultActions result = mockMvc.perform(get("/rooms/{roomId}/recruiting", targetRoom.getRoomId())
                .requestAttr("userId", joiningUser.getUserId()));

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isHost", is(false)))
                .andExpect(jsonPath("$.data.isJoining", is(true)))
                .andExpect(jsonPath("$.data.roomName", is("과학-방-1일뒤-활동시작")))
                .andExpect(jsonPath("$.data.progressStartDate", is(DateUtil.formatDate(LocalDate.now().plusDays(1)))))
                .andExpect(jsonPath("$.data.memberCount", is(4)))
                .andExpect(jsonPath("$.data.recruitCount", is(10)))
                .andExpect(jsonPath("$.data.isbn", is("isbn1")))
                .andExpect(jsonPath("$.data.bookTitle", is("과학-책")))
                .andExpect(jsonPath("$.data.recommendRooms", hasSize(5)))
                /**
                 * recommendRooms 검증 : 현재 조회하는 방과 동일한 카테고리의 다른 방을 추천
                 * <정렬 순서> : 모집 마감 임박 순
                 */
                .andExpect(jsonPath("$.data.recommendRooms[0].roomName", is("방이름입니다")))
                .andExpect(jsonPath("$.data.recommendRooms[1].roomName", is("무슨방일까요??")))
                .andExpect(jsonPath("$.data.recommendRooms[2].roomName", is("과학-방-8일뒤-활동시작")))
                .andExpect(jsonPath("$.data.recommendRooms[3].roomName", is("과학-방-10일뒤-활동시작")))
                .andExpect(jsonPath("$.data.recommendRooms[4].roomName", is("과학-방-15일뒤-활동시작")));
    }

    @Test
    @DisplayName("추천하는 다른 모집중인 모임방이 없을 경우, 해당 데이터를 빈 배열로 반환한다.")
    void get_recruiting_room_detail_no_recommend_rooms() throws Exception {
        //given
        RoomJpaEntity targetRoom = saveScienceRoom("과학-책", "isbn1", "과학-방-1일뒤-활동시작", LocalDate.now().plusDays(1), 10);
        saveUsersToRoom(targetRoom, 4);
        UserJpaEntity joiningUser = userRoomJpaRepository.findAllByRoomJpaEntity_RoomId(targetRoom.getRoomId()).get(1).getUserJpaEntity();

        RoomJpaEntity room_3 = saveLiteratureRoom("문학-책", "isbn5", "방제목에-과학-포함된-문학방", LocalDate.now().plusDays(10), 8);
        saveUsersToRoom(room_3, 6);

        RoomJpaEntity recruit_expired_room_4 = saveScienceRoom("과학-책", "isbn6", "모집기한-지난-과학방", LocalDate.now().minusDays(1), 8);
        saveUsersToRoom(recruit_expired_room_4, 6);

        //when
        ResultActions result = mockMvc.perform(get("/rooms/{roomId}/recruiting", targetRoom.getRoomId())
                .requestAttr("userId", joiningUser.getUserId()));

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isHost", is(false)))
                .andExpect(jsonPath("$.data.isJoining", is(true)))
                .andExpect(jsonPath("$.data.roomName", is("과학-방-1일뒤-활동시작")))
                .andExpect(jsonPath("$.data.progressStartDate", is(DateUtil.formatDate(LocalDate.now().plusDays(1)))))
                .andExpect(jsonPath("$.data.memberCount", is(4)))
                .andExpect(jsonPath("$.data.recruitCount", is(10)))
                .andExpect(jsonPath("$.data.isbn", is("isbn1")))
                .andExpect(jsonPath("$.data.bookTitle", is("과학-책")))
                .andExpect(jsonPath("$.data.recommendRooms", hasSize(0)));
    }
}
