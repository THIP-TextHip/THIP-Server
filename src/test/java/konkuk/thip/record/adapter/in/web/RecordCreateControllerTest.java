package konkuk.thip.record.adapter.in.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.record.adapter.in.web.request.RecordCreateRequest;
import konkuk.thip.record.adapter.out.jpa.RecordJpaEntity;
import konkuk.thip.record.adapter.out.persistence.repository.RecordJpaRepository;
import konkuk.thip.room.adapter.out.jpa.CategoryJpaEntity;
import konkuk.thip.room.adapter.out.jpa.RoomJpaEntity;
import konkuk.thip.room.adapter.out.jpa.RoomParticipantJpaEntity;
import konkuk.thip.room.adapter.out.jpa.RoomParticipantRole;
import konkuk.thip.room.adapter.out.persistence.repository.CategoryJpaRepository;
import konkuk.thip.room.adapter.out.persistence.repository.RoomJpaRepository;
import konkuk.thip.user.adapter.out.jpa.*;
import konkuk.thip.user.adapter.out.persistence.repository.AliasJpaRepository;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.room.adapter.out.persistence.repository.RoomParticipantJpaRepository;
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

import java.util.Map;

import static konkuk.thip.common.exception.code.ErrorCode.API_INVALID_PARAM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("[통합] RecordCommandController 테스트")
class RecordCreateControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
    private RecordJpaRepository recordJpaRepository;

    @Autowired
    private RoomParticipantJpaRepository roomParticipantJpaRepository;

    @AfterEach
    void tearDown() {
        recordJpaRepository.deleteAll();
        roomParticipantJpaRepository.deleteAll();
        roomJpaRepository.deleteAll();
        bookJpaRepository.deleteAll();
        categoryJpaRepository.deleteAll();
        userJpaRepository.deleteAll();
        aliasJpaRepository.deleteAll();
    }

    private void saveUserAndRoom() {
        AliasJpaEntity alias = aliasJpaRepository.save(TestEntityFactory.createLiteratureAlias());

        UserJpaEntity user = userJpaRepository.save(TestEntityFactory.createUser(alias));

        BookJpaEntity book = bookJpaRepository.save(TestEntityFactory.createBook());

        CategoryJpaEntity category = categoryJpaRepository.save(TestEntityFactory.createLiteratureCategory(alias));

        RoomJpaEntity room = roomJpaRepository.save(TestEntityFactory.createRoom(book, category));

        //UserRoomJpaEntity 생성 및 저장
        RoomParticipantJpaEntity userRoom = RoomParticipantJpaEntity.builder()
                .currentPage(10)
                .userPercentage(80.0)
                .roomParticipantRole(RoomParticipantRole.HOST)
                .userJpaEntity(user)
                .roomJpaEntity(room)
                .build();

        roomParticipantJpaRepository.save(userRoom);
    }

    @Test
    @DisplayName("[페이지 넘버, 총평 여부, 기록 내용]을 받아, 기록을 생성한다.")
    void record_create_success() throws Exception {
        //given
        saveUserAndRoom();

        int page = 10;
        boolean isOverview = false;
        String content = "기록 내용";

        RecordCreateRequest request = new RecordCreateRequest(
                page,
                isOverview,
                content
        );

        Long userId = userJpaRepository.findAll().get(0).getUserId();
        Long roomId = roomJpaRepository.findAll().get(0).getRoomId();

        //when
        ResultActions result = mockMvc.perform(post("/rooms/{roomId}/record", roomId)
                .requestAttr("userId", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)
                ));

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.recordId").exists());

        String json = result.andReturn().getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(json);
        Long recordId = jsonNode.path("data").path("recordId").asLong();

        RecordJpaEntity recordJpaEntity = recordJpaRepository.findById(recordId).orElse(null);

        assertThat(recordJpaEntity).isNotNull();
        assertThat(recordJpaEntity.getUserJpaEntity().getUserId()).isEqualTo(userId);
        assertThat(recordJpaEntity.getRoomJpaEntity().getRoomId()).isEqualTo(roomId);
        assertThat(recordJpaEntity.getPage()).isEqualTo(page);
        assertThat(recordJpaEntity.getContent()).isEqualTo(content);
    }

    @Test
    @DisplayName("[page]가 누락되었을 때 400 Bad Request 반환")
    void record_create_page_null() throws Exception {
        // given: page 누락
        Map<String, Object> request = Map.of(
                "isOverview", false,
                "content", "내용"
        );

        // when & then
        mockMvc.perform(post("/rooms/{roomId}/record", 1L)
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(API_INVALID_PARAM.getCode()))
                .andExpect(jsonPath("$.message", containsString("page는 필수입니다.")));
    }

    @Test
    @DisplayName("[isOverview]가 누락되었을 때 400 Bad Request 반환")
    void record_create_is_over_view_null() throws Exception {
        // given: isOverview 누락
        Map<String, Object> request = Map.of(
                "page", 1,
                "content", "내용"
        );

        // when & then
        mockMvc.perform(post("/rooms/{roomId}/record", 1L)
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(API_INVALID_PARAM.getCode()))
                .andExpect(jsonPath("$.message", containsString("isOverview(= 총평 여부)는 필수입니다.")));
    }

    @Test
    @DisplayName("[content]가 빈 문자열일 때 400 Bad Request 반환")
    void record_create_content_blank() throws Exception {
        // given
        Map<String, Object> request = Map.of(
                "page", 1,
                "isOverview", false,
                "content", ""
        );

        // when & then
        mockMvc.perform(post("/rooms/{roomId}/record", 1L)
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(API_INVALID_PARAM.getCode()))
                .andExpect(jsonPath("$.message", containsString("기록 내용은 필수입니다.")));
    }

    @Test
    @DisplayName("[content]가 500자 초과일 때 400 Bad Request 반환")
    void record_create_content_too_long() throws Exception {
        // given
        String longContent = "가".repeat(501);
        Map<String, Object> request = Map.of(
                "page", 1,
                "isOverview", false,
                "content", longContent
        );

        // when & then
        mockMvc.perform(post("/rooms/{roomId}/record", 1L)
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(API_INVALID_PARAM.getCode()))
                .andExpect(jsonPath("$.message", containsString("기록 내용은 최대 500자 입니다.")));
    }

}