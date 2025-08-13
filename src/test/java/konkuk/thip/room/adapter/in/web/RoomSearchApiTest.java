package konkuk.thip.room.adapter.in.web;

import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.room.adapter.out.jpa.CategoryJpaEntity;
import konkuk.thip.room.adapter.out.jpa.RoomJpaEntity;
import konkuk.thip.room.adapter.out.jpa.RoomParticipantJpaEntity;
import konkuk.thip.room.adapter.out.jpa.RoomParticipantRole;
import konkuk.thip.room.adapter.out.persistence.repository.category.CategoryJpaRepository;
import konkuk.thip.room.adapter.out.persistence.repository.RoomJpaRepository;
import konkuk.thip.room.domain.Category;
import konkuk.thip.user.adapter.out.jpa.*;
import konkuk.thip.user.adapter.out.persistence.repository.alias.AliasJpaRepository;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.room.adapter.out.persistence.repository.roomparticipant.RoomParticipantJpaRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.is;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("[통합] 방 검색 api 통합 테스트")
class RoomSearchApiTest {

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
                        .nicknameUpdatedAt(LocalDate.now().minusMonths(7))
                        .oauth2Id("oauth2Id")
                        .role(UserRole.USER)
                        .aliasForUserJpaEntity(alias)
                        .build())
                .toList();

        List<UserJpaEntity> savedUsers = userJpaRepository.saveAll(users);

        // UserRoom 매핑 리스트 생성 및 저장
        List<RoomParticipantJpaEntity> mappings = savedUsers.stream()
                .map(user -> RoomParticipantJpaEntity.builder()
                        .userJpaEntity(user)
                        .roomJpaEntity(roomJpaEntity)
                        .roomParticipantRole(RoomParticipantRole.MEMBER)
                        .build())
                .toList();

        roomParticipantJpaRepository.saveAll(mappings);
    }

    @Test
    @DisplayName("keyword = [과학], 카테고리 선택 X, 정렬 = [마감임박순] 일 경우, 방이름 or 책제목에 '과학'이 포함된 방 검색 결과가 마감임박순으로 반환된다.")
    void search_keyword_and_sort_deadline() throws Exception {
        //given
        RoomJpaEntity science_room_1 = saveScienceRoom("과학-책", "isbn1", "과학-방-1일뒤-활동시작", LocalDate.now().plusDays(1), 10);
        saveUsersToRoom(science_room_1, 4);

        RoomJpaEntity science_room_2 = saveScienceRoom("과학-책", "isbn2", "방이름입니다", LocalDate.now().plusDays(1), 10);
        saveUsersToRoom(science_room_2, 5);

        RoomJpaEntity science_room_3 = saveScienceRoom("과학-책", "isbn3", "무슨방일까요??", LocalDate.now().plusDays(5), 8);
        saveUsersToRoom(science_room_3, 2);

        RoomJpaEntity science_room_4 = saveScienceRoom("과학-책", "isbn4", "과학-방-5일뒤-활동시작", LocalDate.now().plusDays(5), 8);
        saveUsersToRoom(science_room_4, 1);

        RoomJpaEntity room_3 = saveLiteratureRoom("문학-책", "isbn5", "방제목에-과학-포함된-문학방", LocalDate.now().plusDays(10), 8);
        saveUsersToRoom(room_3, 6);

        RoomJpaEntity recruit_expired_room_4 = saveScienceRoom("과학-책", "isbn6", "모집기한-지난-과학방", LocalDate.now().minusDays(1), 8);
        saveUsersToRoom(recruit_expired_room_4, 6);

        //when
        ResultActions result = mockMvc.perform(get("/rooms/search")
                .requestAttr("userId", 1L)
                .param("keyword", "과학")
                .param("sort", "deadline")
                .param("isFinalized", String.valueOf(false))
                .param("page", "1"));

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roomList", hasSize(5)))     // 결과 리스트 크기 확인
                .andExpect(jsonPath("$.data.page", is(1)))      // 페이징 정보 검증
                .andExpect(jsonPath("$.data.size", is(5)))
                .andExpect(jsonPath("$.data.first", is(true)))
                .andExpect(jsonPath("$.data.last", is(true)))
                /**
                 * roomList 검증 : 정렬 순서, 방 검색 결과 검증
                 * <정렬 순서>
                 * 1. 정렬 조건
                 * 2. 정렬 조건이 같을 경우, 방이름에 keyword가 포함된 검색결과가 책제목에 keyword가 포함된 검색결과보다 우선순위가 더 높다
                 */
                .andExpect(jsonPath("$.data.roomList[0].roomName", is("과학-방-1일뒤-활동시작")))
                .andExpect(jsonPath("$.data.roomList[1].roomName", is("방이름입니다")))
                .andExpect(jsonPath("$.data.roomList[2].roomName", is("과학-방-5일뒤-활동시작")))
                .andExpect(jsonPath("$.data.roomList[3].roomName", is("무슨방일까요??")))
                .andExpect(jsonPath("$.data.roomList[4].roomName", is("방제목에-과학-포함된-문학방")));
    }

    @Test
    @DisplayName("keyword = [과학], 카테고리 선택 X, 정렬 = [인기순] 일 경우, 방이름 or 책제목에 '과학'이 포함된 방 검색 결과가 인기순(= 현재까지 모집된 인원 많은 순)으로 반환된다.")
    void search_keyword_and_sort_member_count() throws Exception {
        //given
        RoomJpaEntity science_room_1 = saveScienceRoom("과학-책", "isbn1", "과학-방-1일뒤-활동시작", LocalDate.now().plusDays(1), 10);
        saveUsersToRoom(science_room_1, 4);

        RoomJpaEntity science_room_2 = saveScienceRoom("과학-책", "isbn2", "방이름입니다", LocalDate.now().plusDays(1), 10);
        saveUsersToRoom(science_room_2, 5);

        RoomJpaEntity science_room_3 = saveScienceRoom("과학-책", "isbn3", "무슨방일까요??", LocalDate.now().plusDays(5), 8);
        saveUsersToRoom(science_room_3, 2);

        RoomJpaEntity science_room_4 = saveScienceRoom("과학-책", "isbn4", "과학-방-5일뒤-활동시작", LocalDate.now().plusDays(5), 8);
        saveUsersToRoom(science_room_4, 1);

        RoomJpaEntity room_3 = saveLiteratureRoom("문학-책", "isbn5", "방제목에-과학-포함된-문학방", LocalDate.now().plusDays(10), 8);
        saveUsersToRoom(room_3, 6);

        RoomJpaEntity recruit_expired_room_4 = saveScienceRoom("과학-책", "isbn6", "모집기한-지난-과학방", LocalDate.now().minusDays(1), 8);
        saveUsersToRoom(recruit_expired_room_4, 6);

        //when
        ResultActions result = mockMvc.perform(get("/rooms/search")
                .requestAttr("userId", 1L)
                .param("keyword", "과학")
                .param("sort", "memberCount")
                .param("isFinalized", String.valueOf(false))
                .param("page", "1"));

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roomList", hasSize(5)))     // 결과 리스트 크기 확인
                .andExpect(jsonPath("$.data.page", is(1)))      // 페이징 정보 검증
                .andExpect(jsonPath("$.data.size", is(5)))
                .andExpect(jsonPath("$.data.first", is(true)))
                .andExpect(jsonPath("$.data.last", is(true)))
                /**
                 * roomList 검증 : 정렬 순서, 방 검색 결과 검증
                 * <정렬 순서>
                 * 1. 정렬 조건
                 * 2. 정렬 조건이 같을 경우, 방이름에 keyword가 포함된 검색결과가 책제목에 keyword가 포함된 검색결과보다 우선순위가 더 높다
                 */
                .andExpect(jsonPath("$.data.roomList[0].roomName", is("방제목에-과학-포함된-문학방")))
                .andExpect(jsonPath("$.data.roomList[0].memberCount", is(6)))

                .andExpect(jsonPath("$.data.roomList[1].roomName", is("방이름입니다")))
                .andExpect(jsonPath("$.data.roomList[1].memberCount", is(5)))

                .andExpect(jsonPath("$.data.roomList[2].roomName", is("과학-방-1일뒤-활동시작")))
                .andExpect(jsonPath("$.data.roomList[2].memberCount", is(4)))

                .andExpect(jsonPath("$.data.roomList[3].roomName", is("무슨방일까요??")))
                .andExpect(jsonPath("$.data.roomList[3].memberCount", is(2)))

                .andExpect(jsonPath("$.data.roomList[4].roomName", is("과학-방-5일뒤-활동시작")))
                .andExpect(jsonPath("$.data.roomList[4].memberCount", is(1)));
    }

    @Test
    @DisplayName("keyword 입력 x, 카테고리 = [과학/IT], 정렬 = [마감임박순] 일 경우, [과학/IT] 카테고리에 속하는 방 검색 결과가 반환된다.")
    void search_category_and_sort_deadline() throws Exception {
        //given
        RoomJpaEntity science_room_1 = saveScienceRoom("과학-책", "isbn1", "과학-방-1일뒤-활동시작", LocalDate.now().plusDays(1), 10);
        saveUsersToRoom(science_room_1, 4);

        RoomJpaEntity science_room_3 = saveScienceRoom("과학-책", "isbn3", "무슨방일까요??", LocalDate.now().plusDays(5), 8);
        saveUsersToRoom(science_room_3, 2);

        RoomJpaEntity room_3 = saveLiteratureRoom("문학-책", "isbn5", "방제목에-과학-포함된-문학방", LocalDate.now().plusDays(10), 8);
        saveUsersToRoom(room_3, 6);

        RoomJpaEntity recruit_expired_room_4 = saveScienceRoom("과학-책", "isbn6", "모집기한-지난-과학방", LocalDate.now().minusDays(1), 8);
        saveUsersToRoom(recruit_expired_room_4, 6);

        //when
        ResultActions result = mockMvc.perform(get("/rooms/search")
                .requestAttr("userId", 1L)
                .param("category", "과학·IT")
                .param("sort", "deadline")
                .param("isFinalized", String.valueOf(false))
                .param("page", "1"));

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roomList", hasSize(2)))     // 결과 리스트 크기 확인
                .andExpect(jsonPath("$.data.page", is(1)))      // 페이징 정보 검증
                .andExpect(jsonPath("$.data.size", is(2)))
                .andExpect(jsonPath("$.data.first", is(true)))
                .andExpect(jsonPath("$.data.last", is(true)))
                /**
                 * roomList 검증 : 정렬 순서, 방 검색 결과 검증
                 * <정렬 순서>
                 * 1. 정렬 조건
                 * 2. 정렬 조건이 같을 경우, 방이름에 keyword가 포함된 검색결과가 책제목에 keyword가 포함된 검색결과보다 우선순위가 더 높다
                 */
                .andExpect(jsonPath("$.data.roomList[0].roomName", is("과학-방-1일뒤-활동시작")))
                .andExpect(jsonPath("$.data.roomList[1].roomName", is("무슨방일까요??")));
    }

    @Test
    @DisplayName("keyword 입력 x, 카테고리 입력 x, 정렬 = [마감임박순] 일 경우, DB에 존재하는 전체 방 검색 결과가 반환된다.")
    void search_sort_deadline() throws Exception {
        //given
        RoomJpaEntity science_room_1 = saveScienceRoom("과학-책", "isbn1", "과학-방-1일뒤-활동시작", LocalDate.now().plusDays(1), 10);
        saveUsersToRoom(science_room_1, 4);

        RoomJpaEntity science_room_3 = saveScienceRoom("과학-책", "isbn3", "무슨방일까요??", LocalDate.now().plusDays(5), 8);
        saveUsersToRoom(science_room_3, 2);

        RoomJpaEntity room_3 = saveLiteratureRoom("문학-책", "isbn5", "방제목에-과학-포함된-문학방", LocalDate.now().plusDays(10), 8);
        saveUsersToRoom(room_3, 6);

        RoomJpaEntity recruit_expired_room_4 = saveScienceRoom("과학-책", "isbn6", "모집기한-지난-과학방", LocalDate.now().minusDays(1), 8);
        saveUsersToRoom(recruit_expired_room_4, 6);

        //when
        ResultActions result = mockMvc.perform(get("/rooms/search")
                .requestAttr("userId", 1L)
                .param("sort", "deadline")
                .param("isFinalized", String.valueOf(false))
                .param("page", "1"));

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roomList", hasSize(3)))     // 결과 리스트 크기 확인
                .andExpect(jsonPath("$.data.page", is(1)))      // 페이징 정보 검증
                .andExpect(jsonPath("$.data.size", is(3)))
                .andExpect(jsonPath("$.data.first", is(true)))
                .andExpect(jsonPath("$.data.last", is(true)))
                /**
                 * roomList 검증 : 정렬 순서, 방 검색 결과 검증
                 * <정렬 순서>
                 * 1. 정렬 조건
                 * 2. 정렬 조건이 같을 경우, 방이름에 keyword가 포함된 검색결과가 책제목에 keyword가 포함된 검색결과보다 우선순위가 더 높다
                 */
                .andExpect(jsonPath("$.data.roomList[0].roomName", is("과학-방-1일뒤-활동시작")))
                .andExpect(jsonPath("$.data.roomList[1].roomName", is("무슨방일까요??")))
                .andExpect(jsonPath("$.data.roomList[2].roomName", is("방제목에-과학-포함된-문학방")));
    }

    @Test
    @DisplayName("keyword=[과학], category=[과학/IT], 정렬=[마감임박순] 일 경우, keyword, category 조건을 모두 만족하는 방만 반환된다.")
    void search_keyword_and_category() throws Exception {
        // given
        RoomJpaEntity science_room_1 = saveScienceRoom("과학-책", "isbn1", "과학-방-1일뒤-활동시작", LocalDate.now().plusDays(1), 10);
        saveUsersToRoom(science_room_1, 4);

        RoomJpaEntity science_room_3 = saveScienceRoom("과학-책", "isbn3", "무슨방일까요??", LocalDate.now().plusDays(5), 8);
        saveUsersToRoom(science_room_3, 2);

        RoomJpaEntity room_3 = saveLiteratureRoom("문학-책", "isbn5", "문학방입니다", LocalDate.now().plusDays(10), 8);
        saveUsersToRoom(room_3, 6);

        RoomJpaEntity recruit_expired_room_4 = saveScienceRoom("과학-책", "isbn6", "모집기한-지난-과학방", LocalDate.now().minusDays(1), 8);
        saveUsersToRoom(recruit_expired_room_4, 6);

        // when
        ResultActions result = mockMvc.perform(get("/rooms/search")
                .requestAttr("userId", 1L)
                .param("keyword", "과학")
                .param("category", Category.SCIENCE_IT.getValue())
                .param("sort", "deadline")
                .param("isFinalized", String.valueOf(false))
                .param("page", "1"));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roomList", hasSize(2)))
                .andExpect(jsonPath("$.data.page", is(1)))
                .andExpect(jsonPath("$.data.size", is(2)))
                .andExpect(jsonPath("$.data.first", is(true)))
                .andExpect(jsonPath("$.data.last", is(true)))
                /**
                 * roomList 검증 : 정렬 순서, 방 검색 결과 검증
                 * <정렬 순서>
                 * 1. 정렬 조건
                 * 2. 정렬 조건이 같을 경우, 방이름에 keyword가 포함된 검색결과가 책제목에 keyword가 포함된 검색결과보다 우선순위가 더 높다
                 */
                .andExpect(jsonPath("$.data.roomList[0].roomName", is("과학-방-1일뒤-활동시작")))
                .andExpect(jsonPath("$.data.roomList[1].roomName", is("무슨방일까요??")));
    }
}
