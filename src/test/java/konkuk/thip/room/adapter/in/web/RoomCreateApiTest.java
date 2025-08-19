package konkuk.thip.room.adapter.in.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.room.adapter.out.jpa.RoomJpaEntity;
import konkuk.thip.room.adapter.out.jpa.RoomParticipantJpaEntity;
import konkuk.thip.room.adapter.out.persistence.repository.RoomJpaRepository;
import konkuk.thip.room.adapter.out.persistence.repository.roomparticipant.RoomParticipantJpaRepository;
import konkuk.thip.room.domain.Category;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserRole;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.user.domain.Alias;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("[통합] 방 생성 api 통합 테스트")
class RoomCreateApiTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserJpaRepository userJpaRepository;
    @Autowired private BookJpaRepository bookJpaRepository;
    @Autowired private RoomJpaRepository roomJpaRepository;
    @Autowired private RoomParticipantJpaRepository roomParticipantJpaRepository;

    @AfterEach
    void tearDown() {
        roomParticipantJpaRepository.deleteAllInBatch();
        roomJpaRepository.deleteAllInBatch();
        bookJpaRepository.deleteAllInBatch();
        userJpaRepository.deleteAllInBatch();
    }

    private void saveUserAndLiteratureCategory() {
        Alias alias = TestEntityFactory.createLiteratureAlias();

        userJpaRepository.save(UserJpaEntity.builder()
                .oauth2Id("kakao_432708231")
                .nickname("User1")
                .nicknameUpdatedAt(LocalDate.now().minusMonths(7))
                .role(UserRole.USER)
                .alias(alias)
                .build());

        Category category = TestEntityFactory.createLiteratureCategory();
    }

    private void saveBookWithPageCount() {
        bookJpaRepository.save(BookJpaEntity.builder()
                .title("작별하지 않는다")
                .isbn("9791168342941")      // 실제 isbn 값
                .authorName("박곰희")
                .bestSeller(false)
                .publisher("문학동네")
                .imageUrl("https://image1.jpg")
                .pageCount(296)             // pageCount 값이 null이 아닌 책
                .description("한강의 소설")
                .build());
    }

    private void saveBookWithoutPageCount() {
        bookJpaRepository.save(BookJpaEntity.builder()
                .title("작별하지 않는다")
                .isbn("9791168342941")      // 실제 isbn 값
                .authorName("박곰희")
                .bestSeller(false)
                .publisher("문학동네")
                .imageUrl("https://image1.jpg")
                .pageCount(null)            // pageCount 값이 null 인 책 -> 실제 페이지 정보 296
                .description("한강의 소설")
                .build());
    }

    private Map<String, Object> buildRoomCreateRequest() {
        Map<String, Object> request = new HashMap<>();
        request.put("isbn", "9791168342941");
        request.put("category", "문학");           // 실제 카테고리 값
        request.put("roomName", "방이름");
        request.put("description", "방설명");
        request.put("progressStartDate", LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("yyyy.MM.dd")));
        request.put("progressEndDate", LocalDate.now().plusDays(10).format(DateTimeFormatter.ofPattern("yyyy.MM.dd")));
        request.put("recruitCount", 3);
        request.put("password", null);
        request.put("isPublic", true);
        return request;
    }

    @Test
    @DisplayName("isbn 에 해당하는 책(with pageCount)이 DB에 존재할 때, 해당 책과 연관된 방을 생성할 수 있다.")
    void room_create_book_with_page_exist() throws Exception {
        //given : user, category, pageCount값이 있는 book 생성, request 생성
        saveUserAndLiteratureCategory();
        saveBookWithPageCount();

        Long userId = userJpaRepository.findAll().get(0).getUserId();
        Long bookId = bookJpaRepository.findAll().get(0).getBookId();

        Map<String, Object> request = buildRoomCreateRequest();

        //when
        ResultActions result = mockMvc.perform(post("/rooms")
                .requestAttr("userId", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)
                ));

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roomId").exists());

        String json = result.andReturn().getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(json);
        Long roomId = jsonNode.path("data").path("roomId").asLong();

        RoomJpaEntity roomJpaEntity = roomJpaRepository.findById(roomId).orElse(null);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        LocalDate startDate = LocalDate.parse((String) request.get("progressStartDate"), formatter);
        LocalDate endDate = LocalDate.parse((String) request.get("progressEndDate"), formatter);

        assertThat(roomJpaEntity).isNotNull()
                .extracting(
                        "title", "description", "public", "password", "roomPercentage",
                        "startDate", "endDate", "recruitCount", "bookJpaEntity.bookId", "category"
                )
                .containsExactly(
                        request.get("roomName"), request.get("description"), request.get("isPublic"), request.get("password"), 0.0,
                        startDate, endDate, request.get("recruitCount"), bookId, Category.LITERATURE
                );
    }

    @Test
    @DisplayName("isbn 에 해당하는 책(without pageCount)이 DB에 존재할 때, 해당 책의 page 정보를 update 한 후 연관된 방을 생성할 수 있다.")
    void room_create_book_without_page_exist() throws Exception {
        //given : user, category, pageCount값이 없는 book 생성, request 생성
        saveUserAndLiteratureCategory();
        saveBookWithoutPageCount();

        Long userId = userJpaRepository.findAll().get(0).getUserId();
        Long bookId = bookJpaRepository.findAll().get(0).getBookId();

        Map<String, Object> request = buildRoomCreateRequest();

        //when
        ResultActions result = mockMvc.perform(post("/rooms")
                .requestAttr("userId", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)
                ));

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roomId").exists());

        String json = result.andReturn().getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(json);
        Long roomId = jsonNode.path("data").path("roomId").asLong();

        RoomJpaEntity roomJpaEntity = roomJpaRepository.findById(roomId).orElse(null);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        LocalDate startDate = LocalDate.parse((String) request.get("progressStartDate"), formatter);
        LocalDate endDate = LocalDate.parse((String) request.get("progressEndDate"), formatter);

        assertThat(roomJpaEntity).isNotNull()
                .extracting(
                        "title", "description", "public", "password", "roomPercentage",
                        "startDate", "endDate", "recruitCount", "bookJpaEntity.bookId", "category"
                )
                .containsExactly(
                        request.get("roomName"), request.get("description"), request.get("isPublic"), request.get("password"), 0.0,
                        startDate, endDate, request.get("recruitCount"), bookId, Category.LITERATURE
                );

        // update 된 책 검증
        BookJpaEntity updatedBookJpaEntity = bookJpaRepository.findById(bookId).orElse(null);
        assertThat(updatedBookJpaEntity.getPageCount()).isEqualTo(296);
    }

    @Test
    @DisplayName("isbn 에 해당하는 책이 존재하지 않을 경우, page 정보를 포함하는 책을 save 한 후 연관된 방을 생성할 수 있다.")
    void room_create_book_not_exist() throws Exception {
        //given : user, category 생성, request 생성 (book 생성 X)
        saveUserAndLiteratureCategory();

        Long userId = userJpaRepository.findAll().get(0).getUserId();

        Map<String, Object> request = buildRoomCreateRequest();

        //when
        ResultActions result = mockMvc.perform(post("/rooms")
                .requestAttr("userId", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)
                ));

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roomId").exists());

        String json = result.andReturn().getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(json);
        Long roomId = jsonNode.path("data").path("roomId").asLong();

        RoomJpaEntity roomJpaEntity = roomJpaRepository.findById(roomId).orElse(null);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        LocalDate startDate = LocalDate.parse((String) request.get("progressStartDate"), formatter);
        LocalDate endDate = LocalDate.parse((String) request.get("progressEndDate"), formatter);

        assertThat(roomJpaEntity).isNotNull()
                .extracting(
                        "title", "description", "public", "password", "roomPercentage",
                        "startDate", "endDate", "recruitCount", "category"
                )
                .containsExactly(
                        request.get("roomName"), request.get("description"), request.get("isPublic"), request.get("password"), 0.0,
                        startDate, endDate, request.get("recruitCount"), Category.LITERATURE
                );

        // 새로 DB에 저장된 책 검증
        BookJpaEntity newSavedBookJpaEntity = bookJpaRepository.findAll().get(0);
        assertThat(newSavedBookJpaEntity)
                .extracting(
                        "isbn", "authorName", "pageCount"
                )
                .containsExactly(
                        "9791168342941", "박곰희", 296
                );
    }

    @Test
    @DisplayName("방 생성에 성공하면, 방장의 정보가 DB에 저장된다.")
    @Transactional      // RoomParticipant -> Room, User 의 manyToOne 지연로딩을 위해 추가
    void room_create_room_participant_save_success() throws Exception {
        //given : user, category, pageCount값이 있는 book 생성, request 생성
        saveUserAndLiteratureCategory();
        saveBookWithPageCount();

        Long userId = userJpaRepository.findAll().get(0).getUserId();
        Long bookId = bookJpaRepository.findAll().get(0).getBookId();

        Map<String, Object> request = buildRoomCreateRequest();

        //when
        ResultActions result = mockMvc.perform(post("/rooms")
                .requestAttr("userId", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)
                ));

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roomId").exists());

        RoomParticipantJpaEntity roomParticipantJpaEntity = roomParticipantJpaRepository.findAll().get(0);
        assertThat(roomParticipantJpaEntity.getUserJpaEntity().getUserId()).isEqualTo(userId);

        RoomJpaEntity savedRoomJpaEntity = roomJpaRepository.findAll().get(0);
        assertThat(roomParticipantJpaEntity.getRoomJpaEntity().getRoomId()).isEqualTo(savedRoomJpaEntity.getRoomId());
    }
}
