package konkuk.thip.room.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static konkuk.thip.common.exception.code.ErrorCode.API_INVALID_PARAM;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("[단위] 비공개 방 비밀번호 입력 api controller 단위 테스트")
class RoomVerifyPasswordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Map<String, Object> buildValidRequest() {
        Map<String, Object> request = new HashMap<>();
        request.put("userId", 1L);
        request.put("roomId", 1L);
        request.put("password", "1234");
        return request;
    }

    private void assertBad(Map<String, Object> req, String msg) throws Exception {
        mockMvc.perform(post("/rooms/{roomId}/password", req.get("roomId"))
                        .requestAttr("userId", req.get("userId"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(API_INVALID_PARAM.getCode()))
                .andExpect(jsonPath("$.message", containsString(msg)));
    }

    @Nested
    @DisplayName("비밀번호 검증")
    class PasswordValidation {
        @Test
        @DisplayName("비밀번호가 4자리 숫자가 아닐 때 400 error")
        void invalid_password() throws Exception {
            Map<String, Object> req = buildValidRequest();
            req.put("password", "12ab");
            assertBad(req, "비밀번호는 숫자 4자리여야 합니다.");
        }

        @Test
        @DisplayName("비밀번호가 4자리가 아닐 때 400 error")
        void short_password() throws Exception {
            Map<String, Object> req = buildValidRequest();
            req.put("password", "123");
            assertBad(req, "비밀번호는 숫자 4자리여야 합니다.");
        }

        @Test
        @DisplayName("비밀번호가 빈 값일 때 400 error")
        void blank_password() throws Exception {
            Map<String, Object> req = buildValidRequest();
            req.put("password", "");
            assertBad(req, "파라미터 값 중 유효하지 않은 값이 있습니다. 비밀번호는 필수입니다.");        }
    }

    @Nested
    @DisplayName("roomId 검증")
    class RoomIdValidation {
        @Test
        @DisplayName("roomId가 없을 때 400 error")
        void missing_roomId() throws Exception {
            Map<String, Object> req = buildValidRequest();
            req.remove("roomId");
            mockMvc.perform(post("/rooms//password")
                            .requestAttr("userId", req.get("userId"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().is4xxClientError());
        }
    }
}
