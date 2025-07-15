package konkuk.thip.user.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import konkuk.thip.common.exception.code.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("[단위] 팔로잉 상태 변경 API controller 단위 테스트")
class UserFollowControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Map<String, Object> buildValidRequest() {
        Map<String, Object> req = new HashMap<>();
        req.put("type", true);
        return req;
    }

    private void assertBad(Map<String, Object> req, String msg) throws Exception {
        mockMvc.perform(post("/users/following/{followingUserId}", 2L)
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.API_INVALID_PARAM.getCode()))
                .andExpect(jsonPath("$.message", containsString(msg)));
    }

    @Test
    @DisplayName("type이 null이면 400 에러")
    void null_type() throws Exception {
        Map<String, Object> req = new HashMap<>(); // type 없음
        assertBad(req, "type은 필수 파라미터입니다.");
    }

}