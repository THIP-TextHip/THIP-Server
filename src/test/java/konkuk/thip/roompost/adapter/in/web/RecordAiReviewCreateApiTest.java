package konkuk.thip.roompost.adapter.in.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.common.ai.application.out.GeminiLoadPort;
import konkuk.thip.common.exception.code.ErrorCode;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.room.adapter.out.jpa.RoomJpaEntity;
import konkuk.thip.room.adapter.out.jpa.RoomParticipantJpaEntity;
import konkuk.thip.room.adapter.out.persistence.repository.RoomJpaRepository;
import konkuk.thip.room.adapter.out.persistence.repository.roomparticipant.RoomParticipantJpaRepository;
import konkuk.thip.room.domain.value.Category;
import konkuk.thip.room.domain.value.RoomParticipantRole;
import konkuk.thip.roompost.adapter.out.jpa.RecordJpaEntity;
import konkuk.thip.roompost.adapter.out.persistence.repository.record.RecordJpaRepository;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.user.domain.value.Alias;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("[통합] AI 기반 기록 독후감 생성 API 테스트")
class RecordAiReviewCreateApiTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Autowired UserJpaRepository userJpaRepository;
    @Autowired BookJpaRepository bookJpaRepository;
    @Autowired RoomJpaRepository roomJpaRepository;
    @Autowired RoomParticipantJpaRepository roomParticipantJpaRepository;
    @Autowired RecordJpaRepository recordJpaRepository;

    @MockitoBean
    GeminiLoadPort geminiLoadPort;

    private UserJpaEntity user;
    private RoomJpaEntity room;

    private void saveUserAndRoom() {
        Alias alias = TestEntityFactory.createLiteratureAlias();
        user = userJpaRepository.save(TestEntityFactory.createUser(alias));

        BookJpaEntity book = bookJpaRepository.save(TestEntityFactory.createBook());
        Category category = TestEntityFactory.createLiteratureCategory();
        room = roomJpaRepository.save(TestEntityFactory.createRoom(book, category));

        RoomParticipantJpaEntity participant = RoomParticipantJpaEntity.builder()
                .currentPage(10)
                .userPercentage(80.0)
                .roomParticipantRole(RoomParticipantRole.HOST)
                .userJpaEntity(user)
                .roomJpaEntity(room)
                .build();

        roomParticipantJpaRepository.save(participant);
    }

    private RecordJpaEntity buildRecord(boolean isOverview, int page, String content) {
        return RecordJpaEntity.builder()
                .content(content)
                .userJpaEntity(user)
                .page(page)
                .isOverview(isOverview)
                .commentCount(0)
                .likeCount(0)
                .roomJpaEntity(room)
                .build();
    }

    @Test
    @DisplayName("기록이 2개 이상이면 AI 독후감을 생성하고, 생성 횟수를 1 증가시켜 응답한다.")
    void create_ai_review_success() throws Exception {
        // given
        saveUserAndRoom();

        recordJpaRepository.save(buildRecord(false, 5, "내용-1"));
        recordJpaRepository.save(buildRecord(false, 7, "내용-2"));
        recordJpaRepository.save(buildRecord(true, 9, "총평"));

        // LLM 호출 모킹
        final String AI_CONTENT = "AI가 생성한 독후감 본문";
        given(geminiLoadPort.generateRecordReview(
                any(), ArgumentMatchers.any(), any(), anyInt(), anyInt()
        )).willReturn(AI_CONTENT);

        // when
        ResultActions result = mockMvc.perform(
                post("/rooms/{roomId}/record/ai-review", room.getRoomId())
                        .requestAttr("userId", user.getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.content").value(AI_CONTENT))
                .andExpect(jsonPath("$.data.count").isNumber());

        String json = result.andReturn().getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(json);
        int returnedCount = root.path("data").path("count").asInt();

        UserJpaEntity persisted = userJpaRepository.findById(user.getUserId()).orElseThrow();
        assertThat(returnedCount).isEqualTo(persisted.getRecordReviewCount());
        assertThat(persisted.getRecordReviewCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("기록이 2개 미만이면 에러 코드를 반환한다.")
    void create_ai_review_fail_when_not_enough_records() throws Exception {
        // given
        saveUserAndRoom();

        recordJpaRepository.save(buildRecord(false, 3, "기록"));

        given(geminiLoadPort.generateRecordReview(any(), anyList(), any(), anyInt(), anyInt()))
                .willReturn("호출되지 않음");

        // when
        ResultActions result = mockMvc.perform(
                post("/rooms/{roomId}/record/ai-review", room.getRoomId())
                        .requestAttr("userId", user.getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.RECORD_REVIEW_NOT_ENOUGH_RECORDS.getCode()));
    }

    @Test
    @DisplayName("독후감 생성 횟수가 5회를 초과하면 에러 코드를 반환한다.")
    void create_ai_review_fail_when_exceed_max_count() throws Exception {
        // given
        saveUserAndRoom();
        user.setRecordReviewCount(6);
        userJpaRepository.save(user);

        recordJpaRepository.save(buildRecord(false, 3, "기록1"));
        recordJpaRepository.save(buildRecord(false, 3, "기록2"));
        recordJpaRepository.save(buildRecord(false, 3, "기록3"));

        given(geminiLoadPort.generateRecordReview(any(), anyList(), any(), anyInt(), anyInt()))
                .willReturn("호출되지 않음");

        // when
        ResultActions result = mockMvc.perform(
                post("/rooms/{roomId}/record/ai-review", room.getRoomId())
                        .requestAttr("userId", user.getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.USER_RECORD_REVIEW_COUNT_EXCEEDS_LIMIT.getCode()));
    }
}