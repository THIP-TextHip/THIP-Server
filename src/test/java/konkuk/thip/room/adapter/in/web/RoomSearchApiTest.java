package konkuk.thip.room.adapter.in.web;

import com.jayway.jsonpath.JsonPath;
import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.recentSearch.adapter.out.jpa.RecentSearchJpaEntity;
import konkuk.thip.recentSearch.adapter.out.persistence.repository.RecentSearchJpaRepository;
import konkuk.thip.room.adapter.out.jpa.RoomJpaEntity;
import konkuk.thip.room.adapter.out.persistence.repository.RoomJpaRepository;
import konkuk.thip.room.adapter.out.persistence.repository.roomparticipant.RoomParticipantJpaRepository;
import konkuk.thip.room.domain.value.Category;
import konkuk.thip.room.domain.value.RoomStatus;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.user.domain.value.Alias;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("[통합] 방 검색 api 통합 테스트")
class RoomSearchApiTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserJpaRepository userJpaRepository;
    @Autowired private BookJpaRepository bookJpaRepository;
    @Autowired private RoomJpaRepository roomJpaRepository;
    @Autowired private RoomParticipantJpaRepository roomParticipantJpaRepository;
    @Autowired private RecentSearchJpaRepository recentSearchJpaRepository;
    @Autowired private JdbcTemplate jdbcTemplate;

    @AfterEach
    void tearDown() {
        recentSearchJpaRepository.deleteAllInBatch();
        roomParticipantJpaRepository.deleteAllInBatch();
        roomJpaRepository.deleteAllInBatch();
        bookJpaRepository.deleteAllInBatch();
        userJpaRepository.deleteAllInBatch();
    }

    private RoomJpaEntity saveScienceRecruitingRoom(String bookTitle, String roomName, LocalDate startDate, LocalDate endDate) {
        BookJpaEntity book = bookJpaRepository.save(TestEntityFactory.createBookWithBookTitle(bookTitle));

        Category category = TestEntityFactory.createScienceCategory();
        return roomJpaRepository.save(TestEntityFactory.createCustomRoom(book, category, roomName, startDate, endDate, RoomStatus.RECRUITING));
    }

    private RoomJpaEntity saveScienceProgressRoom(String bookTitle, String roomName, LocalDate startDate, LocalDate endDate) {
        BookJpaEntity book = bookJpaRepository.save(TestEntityFactory.createBookWithBookTitle(bookTitle));

        Category category = TestEntityFactory.createScienceCategory();
        return roomJpaRepository.save(TestEntityFactory.createCustomRoom(book, category, roomName, startDate, endDate, RoomStatus.IN_PROGRESS));
    }

    private RoomJpaEntity saveLiteratureRecruitingRoom(String bookTitle, String roomName, LocalDate startDate, LocalDate endDate) {
        BookJpaEntity book = bookJpaRepository.save(TestEntityFactory.createBookWithBookTitle(bookTitle));

        Category category = TestEntityFactory.createLiteratureCategory();
        return roomJpaRepository.save(TestEntityFactory.createCustomRoom(book, category, roomName, startDate, endDate, RoomStatus.RECRUITING));
    }

    private RoomJpaEntity saveLiteratureProgressRoom(String bookTitle, String roomName, LocalDate startDate, LocalDate endDate) {
        BookJpaEntity book = bookJpaRepository.save(TestEntityFactory.createBookWithBookTitle(bookTitle));

        Category category = TestEntityFactory.createLiteratureCategory();
        return roomJpaRepository.save(TestEntityFactory.createCustomRoom(book, category, roomName, startDate, endDate, RoomStatus.IN_PROGRESS));
    }

    private void updateRoomMemberCount(RoomJpaEntity roomJpaEntity, int count) {
        jdbcTemplate.update(
                "UPDATE rooms SET member_count = ? WHERE room_id = ?",
                count, roomJpaEntity.getRoomId()
        );
    }

    @Test
    @DisplayName("keyword = [과학], 카테고리 선택 X, 정렬 = [마감임박순] 일 경우, 방이름 or 책제목에 '과학'이 포함된 모집중인 방 검색 결과가 마감임박순으로 반환된다.")
    void search_keyword_and_sort_deadline() throws Exception {
        //given
        RoomJpaEntity science_room_1 = saveScienceRecruitingRoom("과학-책", "과학-방-1일뒤-활동시작", LocalDate.now().plusDays(1), LocalDate.now().plusDays(30));
        updateRoomMemberCount(science_room_1, 4);

        RoomJpaEntity science_room_2 = saveScienceRecruitingRoom("과학-책", "방이름입니다", LocalDate.now().plusDays(2), LocalDate.now().plusDays(30));
        updateRoomMemberCount(science_room_2, 5);

        RoomJpaEntity science_room_3 = saveScienceRecruitingRoom("과학-책", "무슨방일까요??", LocalDate.now().plusDays(3), LocalDate.now().plusDays(30));
        updateRoomMemberCount(science_room_3, 2);

        RoomJpaEntity science_room_4 = saveScienceRecruitingRoom("과학-책", "과학-방-5일뒤-활동시작", LocalDate.now().plusDays(5), LocalDate.now().plusDays(30));
        updateRoomMemberCount(science_room_4, 1);

        RoomJpaEntity room_3 = saveLiteratureRecruitingRoom("문학-책", "방제목에-과학-포함된-문학방", LocalDate.now().plusDays(10), LocalDate.now().plusDays(30));
        updateRoomMemberCount(room_3, 6);

        RoomJpaEntity recruit_expired_room_4 = saveScienceProgressRoom("과학-책", "모집기한-지난-과학방", LocalDate.now().minusDays(1), LocalDate.now().plusDays(30));
        updateRoomMemberCount(recruit_expired_room_4, 6);

        //when
        ResultActions result = mockMvc.perform(get("/rooms/search")
                .requestAttr("userId", 1L)
                .param("keyword", "과학")
                .param("sort", "deadline")
                .param("isFinalized", String.valueOf(false)));

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roomList", hasSize(5)))     // 결과 리스트 크기 확인
                .andExpect(jsonPath("$.data.isLast", is(true)))
                /**
                 * roomList 검증 : 정렬 순서, 방 검색 결과 검증
                 * <정렬 순서>
                 * 1. 정렬 조건
                 * 2. 정렬 조건이 같을 경우, roomId 기준 오름차순 정렬 (무한 스크롤 시 누락 & 중복 되는 데이터 발생하지 않도록)
                 */
                .andExpect(jsonPath("$.data.roomList[0].roomName", is("과학-방-1일뒤-활동시작")))
                .andExpect(jsonPath("$.data.roomList[1].roomName", is("방이름입니다")))
                .andExpect(jsonPath("$.data.roomList[2].roomName", is("무슨방일까요??")))
                .andExpect(jsonPath("$.data.roomList[3].roomName", is("과학-방-5일뒤-활동시작")))
                .andExpect(jsonPath("$.data.roomList[4].roomName", is("방제목에-과학-포함된-문학방")));
    }

    @Test
    @DisplayName("keyword = [과학], 카테고리 선택 X, 정렬 = [인기순] 일 경우, 방이름 or 책제목에 '과학'이 포함된 모집중인 방 검색 결과가 인기순(= 현재까지 모집된 인원 많은 순)으로 반환된다.")
    void search_keyword_and_sort_member_count() throws Exception {
        //given
        RoomJpaEntity science_room_1 = saveScienceRecruitingRoom("과학-책", "과학-방-1일뒤-활동시작", LocalDate.now().plusDays(1), LocalDate.now().plusDays(30));
        updateRoomMemberCount(science_room_1, 4);

        RoomJpaEntity science_room_2 = saveScienceRecruitingRoom("과학-책", "방이름입니다", LocalDate.now().plusDays(2), LocalDate.now().plusDays(30));
        updateRoomMemberCount(science_room_2, 5);

        RoomJpaEntity science_room_3 = saveScienceRecruitingRoom("과학-책", "무슨방일까요??", LocalDate.now().plusDays(3), LocalDate.now().plusDays(30));
        updateRoomMemberCount(science_room_3, 2);

        RoomJpaEntity science_room_4 = saveScienceRecruitingRoom("과학-책", "과학-방-5일뒤-활동시작", LocalDate.now().plusDays(5), LocalDate.now().plusDays(30));
        updateRoomMemberCount(science_room_4, 1);

        RoomJpaEntity room_3 = saveLiteratureRecruitingRoom("문학-책", "방제목에-과학-포함된-문학방", LocalDate.now().plusDays(10), LocalDate.now().plusDays(30));
        updateRoomMemberCount(room_3, 6);

        RoomJpaEntity recruit_expired_room_4 = saveScienceProgressRoom("과학-책", "모집기한-지난-과학방", LocalDate.now().minusDays(1), LocalDate.now().plusDays(30));
        updateRoomMemberCount(recruit_expired_room_4, 6);

        //when
        ResultActions result = mockMvc.perform(get("/rooms/search")
                .requestAttr("userId", 1L)
                .param("keyword", "과학")
                .param("sort", "memberCount")
                .param("isFinalized", String.valueOf(false)));

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roomList", hasSize(5)))     // 결과 리스트 크기 확인
                .andExpect(jsonPath("$.data.isLast", is(true)))
                /**
                 * roomList 검증 : 정렬 순서, 방 검색 결과 검증
                 * <정렬 순서>
                 * 1. 정렬 조건
                 * 2. 정렬 조건이 같을 경우, roomId 기준 오름차순 정렬 (무한 스크롤 시 누락 & 중복 되는 데이터 발생하지 않도록)
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
        RoomJpaEntity science_room_1 = saveScienceRecruitingRoom("과학-책", "과학-방-1일뒤-활동시작", LocalDate.now().plusDays(1), LocalDate.now().plusDays(30));
        updateRoomMemberCount(science_room_1, 4);

        RoomJpaEntity science_room_3 = saveScienceRecruitingRoom("과학-책", "무슨방일까요??", LocalDate.now().plusDays(3), LocalDate.now().plusDays(30));
        updateRoomMemberCount(science_room_3, 2);

        RoomJpaEntity room_3 = saveLiteratureRecruitingRoom("문학-책", "방제목에-과학-포함된-문학방", LocalDate.now().plusDays(10), LocalDate.now().plusDays(30));
        updateRoomMemberCount(room_3, 6);

        RoomJpaEntity recruit_expired_room_4 = saveScienceProgressRoom("과학-책", "모집기한-지난-과학방", LocalDate.now().minusDays(1), LocalDate.now().plusDays(30));
        updateRoomMemberCount(recruit_expired_room_4, 6);

        //when
        ResultActions result = mockMvc.perform(get("/rooms/search")
                .requestAttr("userId", 1L)
                .param("category", Category.SCIENCE_IT.getValue())
                .param("sort", "deadline")
                .param("isFinalized", String.valueOf(false)));

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roomList", hasSize(2)))     // 결과 리스트 크기 확인
                .andExpect(jsonPath("$.data.isLast", is(true)))
                /**
                 * roomList 검증 : 정렬 순서, 방 검색 결과 검증
                 * <정렬 순서>
                 * 1. 정렬 조건
                 * 2. 정렬 조건이 같을 경우, roomId 기준 오름차순 정렬 (무한 스크롤 시 누락 & 중복 되는 데이터 발생하지 않도록)
                 */
                .andExpect(jsonPath("$.data.roomList[0].roomName", is("과학-방-1일뒤-활동시작")))
                .andExpect(jsonPath("$.data.roomList[1].roomName", is("무슨방일까요??")));
    }

    @Test
    @DisplayName("keyword 입력 x, 카테고리 입력 x, 정렬 = [마감임박순] 일 경우, DB에 존재하는 전체 방 검색 결과가 반환된다.")
    void search_sort_deadline() throws Exception {
        //given
        RoomJpaEntity science_room_1 = saveScienceRecruitingRoom("과학-책", "과학-방-1일뒤-활동시작", LocalDate.now().plusDays(1), LocalDate.now().plusDays(30));
        updateRoomMemberCount(science_room_1, 4);

        RoomJpaEntity science_room_3 = saveScienceRecruitingRoom("과학-책", "무슨방일까요??", LocalDate.now().plusDays(3), LocalDate.now().plusDays(30));
        updateRoomMemberCount(science_room_3, 2);

        RoomJpaEntity room_3 = saveLiteratureRecruitingRoom("문학-책", "방제목에-과학-포함된-문학방", LocalDate.now().plusDays(10), LocalDate.now().plusDays(30));
        updateRoomMemberCount(room_3, 6);

        RoomJpaEntity recruit_expired_room_4 = saveScienceProgressRoom("과학-책", "모집기한-지난-과학방", LocalDate.now().minusDays(1), LocalDate.now().plusDays(30));
        updateRoomMemberCount(recruit_expired_room_4, 6);

        //when
        ResultActions result = mockMvc.perform(get("/rooms/search")
                .requestAttr("userId", 1L)
                .param("sort", "deadline")
                .param("isFinalized", String.valueOf(false)));

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roomList", hasSize(3)))     // 결과 리스트 크기 확인
                .andExpect(jsonPath("$.data.isLast", is(true)))
                /**
                 * roomList 검증 : 정렬 순서, 방 검색 결과 검증
                 * <정렬 순서>
                 * 1. 정렬 조건
                 * 2. 정렬 조건이 같을 경우, roomId 기준 오름차순 정렬 (무한 스크롤 시 누락 & 중복 되는 데이터 발생하지 않도록)
                 */
                .andExpect(jsonPath("$.data.roomList[0].roomName", is("과학-방-1일뒤-활동시작")))
                .andExpect(jsonPath("$.data.roomList[1].roomName", is("무슨방일까요??")))
                .andExpect(jsonPath("$.data.roomList[2].roomName", is("방제목에-과학-포함된-문학방")));
    }

    @Test
    @DisplayName("keyword=[과학], category=[과학/IT], 정렬=[마감임박순] 일 경우, keyword, category 조건을 모두 만족하는 방만 반환된다.")
    void search_keyword_and_category() throws Exception {
        // given
        RoomJpaEntity science_room_1 = saveScienceRecruitingRoom("과학-책", "과학-방-1일뒤-활동시작", LocalDate.now().plusDays(1), LocalDate.now().plusDays(30));
        updateRoomMemberCount(science_room_1, 4);

        RoomJpaEntity science_room_3 = saveScienceRecruitingRoom("과학-책", "무슨방일까요??", LocalDate.now().plusDays(3), LocalDate.now().plusDays(30));
        updateRoomMemberCount(science_room_3, 2);

        RoomJpaEntity room_3 = saveLiteratureRecruitingRoom("문학-책", "방제목에-과학-포함된-문학방", LocalDate.now().plusDays(10), LocalDate.now().plusDays(30));
        updateRoomMemberCount(room_3, 6);

        RoomJpaEntity recruit_expired_room_4 = saveScienceProgressRoom("과학-책", "모집기한-지난-과학방", LocalDate.now().minusDays(1), LocalDate.now().plusDays(30));
        updateRoomMemberCount(recruit_expired_room_4, 6);

        // when
        ResultActions result = mockMvc.perform(get("/rooms/search")
                .requestAttr("userId", 1L)
                .param("keyword", "과학")
                .param("category", Category.SCIENCE_IT.getValue())
                .param("sort", "deadline")
                .param("isFinalized", String.valueOf(false)));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roomList", hasSize(2)))
                .andExpect(jsonPath("$.data.isLast", is(true)))
                /**
                 * roomList 검증 : 정렬 순서, 방 검색 결과 검증
                 * <정렬 순서>
                 * 1. 정렬 조건
                 * 2. 정렬 조건이 같을 경우, roomId 기준 오름차순 정렬 (무한 스크롤 시 누락 & 중복 되는 데이터 발생하지 않도록)
                 */
                .andExpect(jsonPath("$.data.roomList[0].roomName", is("과학-방-1일뒤-활동시작")))
                .andExpect(jsonPath("$.data.roomList[1].roomName", is("무슨방일까요??")));
    }

    @Test
    @DisplayName("finalized가 true이면 최근 검색어 목록으로 저장된다.")
    void search_keyword_saved() throws Exception {
        // given
        Alias aliasJpa = TestEntityFactory.createScienceAlias();
        UserJpaEntity me = userJpaRepository.save(TestEntityFactory.createUser(aliasJpa));
        RoomJpaEntity science_room_1 = saveScienceRecruitingRoom("과학-책", "과학-방-1일뒤-활동시작", LocalDate.now().plusDays(1), LocalDate.now().plusDays(30));
        updateRoomMemberCount(science_room_1, 4);

        // when
        ResultActions result = mockMvc.perform(get("/rooms/search")
                .requestAttr("userId", me.getUserId())
                .param("keyword", "과학")
                .param("sort", "deadline")
                .param("isFinalized", String.valueOf(true)));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roomList", hasSize(1)))
                .andExpect(jsonPath("$.data.isLast", is(true)))
                /**
                 * roomList 검증 : 정렬 순서, 방 검색 결과 검증
                 * <정렬 순서>
                 * 1. 정렬 조건
                 * 2. 정렬 조건이 같을 경우, roomId 기준 오름차순 정렬 (무한 스크롤 시 누락 & 중복 되는 데이터 발생하지 않도록)
                 */
                .andExpect(jsonPath("$.data.roomList[0].roomName", is("과학-방-1일뒤-활동시작")));

        RecentSearchJpaEntity recentSearchJpaEntity = recentSearchJpaRepository.findAll().get(0);
        assertThat(recentSearchJpaEntity.getSearchTerm()).isEqualTo("과학");
        assertThat(recentSearchJpaEntity.getUserJpaEntity().getUserId()).isEqualTo(me.getUserId());
    }

    @Test
    @DisplayName("검색결과에 해당하는 방이 많은 경우, 정렬 조건 기준으로 페이징 처리한다.")
    void comment_show_all_page_test() throws Exception {
        //given
        RoomJpaEntity science_room_1 = saveScienceRecruitingRoom("과학-책", "과학-방-1일뒤-활동시작", LocalDate.now().plusDays(1), LocalDate.now().plusDays(30));
        RoomJpaEntity science_room_2 = saveScienceRecruitingRoom("과학-책", "과학-방-2일뒤-활동시작", LocalDate.now().plusDays(2), LocalDate.now().plusDays(30));
        RoomJpaEntity science_room_3 = saveScienceRecruitingRoom("과학-책", "과학-방-3일뒤-활동시작", LocalDate.now().plusDays(3), LocalDate.now().plusDays(30));
        RoomJpaEntity science_room_4 = saveScienceRecruitingRoom("과학-책", "과학-방-4일뒤-활동시작", LocalDate.now().plusDays(4), LocalDate.now().plusDays(30));
        RoomJpaEntity science_room_5 = saveScienceRecruitingRoom("과학-책", "과학-방-5일뒤-활동시작", LocalDate.now().plusDays(5), LocalDate.now().plusDays(30));
        RoomJpaEntity science_room_6 = saveScienceRecruitingRoom("과학-책", "과학-방-6일뒤-활동시작", LocalDate.now().plusDays(6), LocalDate.now().plusDays(30));
        RoomJpaEntity science_room_7 = saveScienceRecruitingRoom("과학-책", "과학-방-7일뒤-활동시작", LocalDate.now().plusDays(7), LocalDate.now().plusDays(30));
        RoomJpaEntity science_room_8 = saveScienceRecruitingRoom("과학-책", "과학-방-8일뒤-활동시작", LocalDate.now().plusDays(8), LocalDate.now().plusDays(30));
        RoomJpaEntity science_room_9 = saveScienceRecruitingRoom("과학-책", "과학-방-9일뒤-활동시작", LocalDate.now().plusDays(9), LocalDate.now().plusDays(30));
        RoomJpaEntity science_room_10 = saveScienceRecruitingRoom("과학-책", "과학-방-10일뒤-활동시작", LocalDate.now().plusDays(10), LocalDate.now().plusDays(30));
        RoomJpaEntity science_room_11 = saveScienceRecruitingRoom("과학-책", "과학-방-11일뒤-활동시작", LocalDate.now().plusDays(11), LocalDate.now().plusDays(30));
        RoomJpaEntity science_room_12 = saveScienceRecruitingRoom("과학-책", "과학-방-12일뒤-활동시작", LocalDate.now().plusDays(12), LocalDate.now().plusDays(30));

        //when //then
        MvcResult firstResult = mockMvc.perform(get("/rooms/search")
                        .requestAttr("userId", 1L)
                        .param("keyword", "과학")
                        .param("sort", "deadline")
                        .param("isFinalized", String.valueOf(false)))
                .andExpect(jsonPath("$.data.nextCursor", notNullValue()))
                .andExpect(jsonPath("$.data.isLast", is(false)))
                .andExpect(jsonPath("$.data.roomList", hasSize(10)))
                /**
                 * roomList 검증 : 정렬 순서, 방 검색 결과 검증
                 * <정렬 순서>
                 * 1. 정렬 조건
                 * 2. 정렬 조건이 같을 경우, roomId 기준 오름차순 정렬 (무한 스크롤 시 누락 & 중복 되는 데이터 발생하지 않도록)
                 */
                .andExpect(jsonPath("$.data.roomList[0].roomName", is("과학-방-1일뒤-활동시작")))
                .andExpect(jsonPath("$.data.roomList[1].roomName", is("과학-방-2일뒤-활동시작")))
                .andExpect(jsonPath("$.data.roomList[2].roomName", is("과학-방-3일뒤-활동시작")))
                .andExpect(jsonPath("$.data.roomList[3].roomName", is("과학-방-4일뒤-활동시작")))
                .andExpect(jsonPath("$.data.roomList[4].roomName", is("과학-방-5일뒤-활동시작")))
                .andExpect(jsonPath("$.data.roomList[5].roomName", is("과학-방-6일뒤-활동시작")))
                .andExpect(jsonPath("$.data.roomList[6].roomName", is("과학-방-7일뒤-활동시작")))
                .andExpect(jsonPath("$.data.roomList[7].roomName", is("과학-방-8일뒤-활동시작")))
                .andExpect(jsonPath("$.data.roomList[8].roomName", is("과학-방-9일뒤-활동시작")))
                .andExpect(jsonPath("$.data.roomList[9].roomName", is("과학-방-10일뒤-활동시작")))
                .andReturn();

        String responseBody = firstResult.getResponse().getContentAsString();
        String nextCursor = JsonPath.read(responseBody, "$.data.nextCursor");

        mockMvc.perform(get("/rooms/search")
                        .requestAttr("userId", 1L)
                        .param("keyword", "과학")
                        .param("sort", "deadline")
                        .param("isFinalized", String.valueOf(false))
                        .param("cursor", nextCursor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isLast", is(true)))
                .andExpect(jsonPath("$.data.roomList", hasSize(2)))
                /**
                 * roomList 검증 : 정렬 순서, 방 검색 결과 검증
                 * <정렬 순서>
                 * 1. 정렬 조건
                 * 2. 정렬 조건이 같을 경우, roomId 기준 오름차순 정렬 (무한 스크롤 시 누락 & 중복 되는 데이터 발생하지 않도록)
                 */
                .andExpect(jsonPath("$.data.roomList[0].roomName", is("과학-방-11일뒤-활동시작")))
                .andExpect(jsonPath("$.data.roomList[1].roomName", is("과학-방-12일뒤-활동시작")));
    }
}
