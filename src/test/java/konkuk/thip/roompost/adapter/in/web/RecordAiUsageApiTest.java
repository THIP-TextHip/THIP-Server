package konkuk.thip.roompost.adapter.in.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.room.adapter.out.jpa.RoomJpaEntity;
import konkuk.thip.room.adapter.out.persistence.repository.RoomJpaRepository;
import konkuk.thip.room.domain.value.Category;
import konkuk.thip.room.adapter.out.jpa.RoomParticipantJpaEntity;
import konkuk.thip.room.adapter.out.persistence.repository.roomparticipant.RoomParticipantJpaRepository;
import konkuk.thip.room.domain.value.RoomParticipantRole;
import konkuk.thip.roompost.adapter.out.jpa.RecordJpaEntity;
import konkuk.thip.roompost.adapter.out.persistence.repository.record.RecordJpaRepository;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("[통합] 사용자의 AI 이용 횟수 및 기록 작성 횟수 조회 API 테스트")
class RecordAiUsageApiTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Autowired UserJpaRepository userJpaRepository;
    @Autowired BookJpaRepository bookJpaRepository;
    @Autowired RoomJpaRepository roomJpaRepository;
    @Autowired RoomParticipantJpaRepository roomParticipantJpaRepository;
    @Autowired RecordJpaRepository recordJpaRepository;

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
    @DisplayName("방 참여자의 일반 기록 개수(isOverview=false)와 사용자 AI 이용 횟수를 조회한다.")
    void get_user_ai_usage_success() throws Exception {
        // given
        saveUserAndRoom();

        // 일반 기록 3건(isOverview=false)
        recordJpaRepository.save(buildRecord(false, 5, "기록-1"));
        recordJpaRepository.save(buildRecord(false, 6, "기록-2"));
        recordJpaRepository.save(buildRecord(false, 7, "기록-3"));

        // 총평 기록 2건(isOverview=true) - 집계 제외 대상
        recordJpaRepository.save(buildRecord(true, 8, "총평-1"));
        recordJpaRepository.save(buildRecord(true, 9, "총평-2"));

        // when
        ResultActions result = mockMvc.perform(
                get("/rooms/{roomId}/users/ai-usage", room.getRoomId())
                        .requestAttr("userId", user.getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.recordCount").value(3))
                .andExpect(jsonPath("$.data.recordReviewCount").isNumber());

        String json = result.andReturn().getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(json);
        int aiUsageCount = root.path("data").path("recordReviewCount").asInt();

        UserJpaEntity persisted = userJpaRepository.findById(user.getUserId()).orElseThrow();
        assertThat(aiUsageCount).isEqualTo(persisted.getRecordReviewCount());
    }

    @Test
    @DisplayName("총평 기록(isOverview=true)은 기록 개수 집계에서 제외된다.")
    void overview_records_are_excluded_from_count() throws Exception {
        // given
        saveUserAndRoom();

        // isOverview=true 만 저장 (집계 기대값: 0)
        recordJpaRepository.save(buildRecord(true, 11, "총평-전용"));

        // when
        ResultActions result = mockMvc.perform(
                get("/rooms/{roomId}/users/ai-usage", room.getRoomId())
                        .requestAttr("userId", user.getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.recordCount").value(0));
    }
}