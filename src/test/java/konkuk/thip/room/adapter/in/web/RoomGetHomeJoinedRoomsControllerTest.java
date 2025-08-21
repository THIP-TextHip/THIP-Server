package konkuk.thip.room.adapter.in.web;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static konkuk.thip.common.exception.code.ErrorCode.API_INVALID_PARAM;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("[단위] 모임 홈 참여중인 내 모임방 조회 api controller 테스트")
class RoomGetHomeJoinedRoomsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private Map<String, Object> buildValidRequest() {
        Map<String, Object> request = new HashMap<>();
        request.put("userId", 1L);
        request.put("page", 1);
        return request;
    }

    private void assertBad(Map<String, Object> req, String msg) throws Exception {
        mockMvc.perform(get("/rooms/home/joined")
                        .requestAttr("userId", req.get("userId"))
                        .param("page", req.get("page") != null ? req.get("page").toString() : null)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(API_INVALID_PARAM.getCode()))
                .andExpect(jsonPath("$.message", containsString(msg)));
    }

    @Nested
    @DisplayName("page 파라미터 검증")
    class PageValidation {

        @Test
        @DisplayName("page가 1 미만일 때 400 error")
        void invalid_page() throws Exception {
            Map<String, Object> req = buildValidRequest();
            req.put("page", 0);
            assertBad(req, "page은 1 이상의 값이어야 합니다.");
        }

        @Test
        @DisplayName("page가 null일 때 400 error")
        void missing_page() throws Exception {
            Map<String, Object> req = buildValidRequest();
            req.remove("page");
            mockMvc.perform(get("/rooms/home/joined")
                            .requestAttr("userId", req.get("userId"))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }
    }

}