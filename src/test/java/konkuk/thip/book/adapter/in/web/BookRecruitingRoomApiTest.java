package konkuk.thip.book.adapter.in.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.room.adapter.out.jpa.RoomJpaEntity;
import konkuk.thip.room.adapter.out.persistence.repository.RoomJpaRepository;
import konkuk.thip.room.domain.value.Category;
import konkuk.thip.room.domain.value.RoomStatus;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.domain.value.UserRole;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.user.domain.value.Alias;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("[통합] 특정 책으로 모집중인 모임방을 조회하는 api 통합 테스트")
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class BookRecruitingRoomApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookJpaRepository bookJpaRepository;

    @Autowired
    private RoomJpaRepository roomJpaRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Test
    @DisplayName("모집 중인 방 목록 조회 성공")
    void getRecruitingRoomsByIsbn_success() throws Exception {
        // given
        BookJpaEntity book = bookJpaRepository.save(BookJpaEntity.builder()
                .isbn("1234567890123")
                .title("모집책")
                .authorName("저자")
                .publisher("출판사")
                .pageCount(300)
                .description("설명")
                .imageUrl("http://image.com")
                .bestSeller(false)
                .build());

        Alias alias = TestEntityFactory.createLiteratureAlias();

        UserJpaEntity user = userJpaRepository.save(UserJpaEntity.builder()
                .oauth2Id("kakao_999")
                .nickname("유저")
                .nicknameUpdatedAt(LocalDate.now().minusMonths(6))
                .role(UserRole.USER)
                .alias(alias)
                .build());

        Category category = TestEntityFactory.createLiteratureCategory();

        RoomJpaEntity recruitingRoom = roomJpaRepository.save(RoomJpaEntity.builder()
                .title("모집 중 방")
                .description("모집 중 설명")
                .isPublic(true)
                .roomPercentage(0.0)
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(30))
                .roomStatus(RoomStatus.RECRUITING)
                .recruitCount(5)
                .bookJpaEntity(book)
                .category(category)
                .build());

        RoomJpaEntity nonRecruitingRoom = roomJpaRepository.save(RoomJpaEntity.builder()
                .title("마감된 방")
                .description("마감 설명")
                .isPublic(true)
                .roomPercentage(0.0)
                .startDate(LocalDate.now().minusDays(30))
                .endDate(LocalDate.now().minusDays(1))
                .roomStatus(RoomStatus.IN_PROGRESS)
                .recruitCount(5)
                .bookJpaEntity(book)
                .category(category)
                .build());

        // when & then
        mockMvc.perform(get("/books/{isbn}/recruiting-rooms", book.getIsbn())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data.recruitingRoomList").isArray())
                .andExpect(jsonPath("$.data.recruitingRoomList[0].roomId").value(recruitingRoom.getRoomId()))
                .andExpect(jsonPath("$.data.recruitingRoomList[0].roomName").value("모집 중 방"))
                .andExpect(jsonPath("$.data.recruitingRoomList.length()").value(1)); // 모집 중인 방만 포함
    }

    @Test
    @DisplayName("ISBN에 해당하는 책이 없는 경우 빈 리스트 반환")
    void getRecruitingRooms_no_matching_book() throws Exception {
        // when & then
        mockMvc.perform(get("/books/{isbn}/recruiting-rooms", "0987654321123")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data.recruitingRoomList").isArray())
                .andExpect(jsonPath("$.data.recruitingRoomList.length()").value(0));
    }

    @Test
    @DisplayName("모집 중인 방 - 커서 기반 페이징 동작 확인")
    void getRecruitingRoomsWithCursor_success() throws Exception {
        // given
        String isbn = "1234567890123";
        BookJpaEntity book = bookJpaRepository.save(TestEntityFactory.createBookWithISBN(isbn));
        Alias alias = TestEntityFactory.createLiteratureAlias();
        Category category = TestEntityFactory.createLiteratureCategory();

        for (int i = 0; i < 15; i++) {
            roomJpaRepository.save(TestEntityFactory.createCustomRoom(
                    book,
                    category,
                    LocalDate.now().plusDays(i + 1),
                    LocalDate.now().plusDays(i + 2),
                    RoomStatus.RECRUITING
            ));
        }

        // when & then (1페이지 요청)
        MvcResult result = mockMvc.perform(get("/books/{isbn}/recruiting-rooms", isbn)
                        .param("cursor", (String) null))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data.recruitingRoomList.length()").value(10)) // pageSize = 10
                .andExpect(jsonPath("$.data.nextCursor").isNotEmpty())
                .andExpect(jsonPath("$.data.isLast").value(false))
                .andReturn();

        // 응답에서 nextCursor 추출
        String jsonResponse = result.getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(jsonResponse);
        String nextCursor = root.path("data").path("nextCursor").asText();

        // when & then (2페이지 요청)
        mockMvc.perform(get("/books/{isbn}/recruiting-rooms", isbn)
                        .param("cursor", nextCursor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.recruitingRoomList.length()").value(5)) // 나머지 5개
                .andExpect(jsonPath("$.data.nextCursor").doesNotExist())
                .andExpect(jsonPath("$.data.isLast").value(true));
    }
}
