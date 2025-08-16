package konkuk.thip.roompost.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static konkuk.thip.common.exception.code.ErrorCode.API_INVALID_PARAM;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("[단위] 투표 생성 api controller 단위 테스트")
class VoteCreateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("[page]가 누락되었을 때 400 Bad Request 반환")
    void vote_create_page_null() throws Exception {
        // given: page 누락
        Map<String, Object> request = Map.of(
                "isOverview", false,
                "content", "내용",
                "voteItemList", List.of(Map.of("itemName", "찬성"))
        );

        // when & then
        mockMvc.perform(post("/rooms/{roomId}/vote", 1L)
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(API_INVALID_PARAM.getCode()))
                .andExpect(jsonPath("$.message", containsString("page는 필수입니다.")));
    }

    @Test
    @DisplayName("[isOverview]가 누락되었을 때 400 Bad Request 반환")
    void vote_create_is_over_view_null() throws Exception {
        // given: isOverview 누락
        Map<String, Object> request = Map.of(
                "page", 1,
                "content", "내용",
                "voteItemList", List.of(Map.of("itemName", "찬성"))
        );

        // when & then
        mockMvc.perform(post("/rooms/{roomId}/vote", 1L)
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(API_INVALID_PARAM.getCode()))
                .andExpect(jsonPath("$.message", containsString("isOverview(= 총평 여부)는 필수입니다.")));
    }

    @Test
    @DisplayName("[content]가 빈 문자열일 때 400 Bad Request 반환")
    void vote_create_content_blank() throws Exception {
        // given
        Map<String, Object> request = Map.of(
                "page", 1,
                "isOverview", false,
                "content", "",
                "voteItemList", List.of(Map.of("itemName", "찬성"))
        );

        // when & then
        mockMvc.perform(post("/rooms/{roomId}/vote", 1L)
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(API_INVALID_PARAM.getCode()))
                .andExpect(jsonPath("$.message", containsString("투표 내용은 필수입니다.")));
    }

    @Test
    @DisplayName("[content]가 20자 초과일 때 400 Bad Request 반환")
    void vote_create_content_too_long() throws Exception {
        // given
        String longContent = "가".repeat(21);
        Map<String, Object> request = Map.of(
                "page", 1,
                "isOverview", false,
                "content", longContent,
                "voteItemList", List.of(Map.of("itemName", "찬성"))
        );

        // when & then
        mockMvc.perform(post("/rooms/{roomId}/vote", 1L)
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(API_INVALID_PARAM.getCode()))
                .andExpect(jsonPath("$.message", containsString("투표 내용은 최대 20자 입니다.")));
    }

    @Test
    @DisplayName("[voteItemList]가 누락되었을 때 400 Bad Request 반환")
    void vote_create_vote_item_null() throws Exception {
        // given
        Map<String, Object> request = Map.of(
                "page", 1,
                "isOverview", false,
                "content", "내용"
                // voteItemList 생략
        );

        // when & then
        mockMvc.perform(post("/rooms/{roomId}/vote", 1L)
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(API_INVALID_PARAM.getCode()))
                .andExpect(jsonPath("$.message", containsString("투표 항목은 필수입니다.")));
    }

    @Test
    @DisplayName("[voteItemList]가 5개 초과일 때 400 Bad Request 반환")
    void vote_create_vote_item_too_many() throws Exception {
        // given: 6개 아이템
        List<Map<String, String>> items = List.of(
                Map.of("itemName","A"), Map.of("itemName","B"),
                Map.of("itemName","C"), Map.of("itemName","D"),
                Map.of("itemName","E"), Map.of("itemName","F")
        );
        Map<String, Object> request = Map.of(
                "page", 1,
                "isOverview", false,
                "content", "내용",
                "voteItemList", items
        );

        // when & then
        mockMvc.perform(post("/rooms/{roomId}/vote", 1L)
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(API_INVALID_PARAM.getCode()))
                .andExpect(jsonPath("$.message", containsString("투표 항목은 1개 이상, 최대 5개까지입니다.")));
    }

    @Test
    @DisplayName("[voteItemList] 내 [itemName]이 빈 문자열일 때 400 Bad Request 반환")
    void vote_create_vote_item_name_blank() throws Exception {
        // given
        Map<String, Object> request = Map.of(
                "page", 1,
                "isOverview", false,
                "content", "내용",
                "voteItemList", List.of(Map.of("itemName",""))
        );

        // when & then
        mockMvc.perform(post("/rooms/{roomId}/vote", 1L)
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(API_INVALID_PARAM.getCode()))
                .andExpect(jsonPath("$.message", containsString("투표 항목 이름은 필수입니다.")));
    }

    @Test
    @DisplayName("[voteItemList] 내 [itemName]이 20자 초과일 때 400 Bad Request 반환")
    void vote_create_vote_item_name_too_long() throws Exception {
        // given
        String longName = "가".repeat(21);
        Map<String, Object> request = Map.of(
                "page", 1,
                "isOverview", false,
                "content", "내용",
                "voteItemList", List.of(Map.of("itemName", longName))
        );

        // when & then
        mockMvc.perform(post("/rooms/{roomId}/vote", 1L)
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(API_INVALID_PARAM.getCode()))
                .andExpect(jsonPath("$.message", containsString("투표 항목 이름은 최대 20자입니다.")));
    }
}
