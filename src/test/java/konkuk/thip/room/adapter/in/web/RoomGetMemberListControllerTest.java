package konkuk.thip.room.adapter.in.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

import static konkuk.thip.common.exception.code.ErrorCode.*;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("[단위] 독서메이트(방 멤버) 조회 api controller 테스트")
class RoomGetMemberListControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private Map<String, Object> buildValidRequest() {
        Map<String, Object> request = new HashMap<>();
        request.put("roomId", 1L);
        return request;
    }

    private void assertRoomNotFound(Map<String, Object> req, String msg) throws Exception {
        mockMvc.perform(get("/rooms/{roomId}/users", req.get("roomId"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ROOM_NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message", containsString(msg)));
    }

    @Nested
    @DisplayName("roomId 검증")
    class RoomIdValidation {

        @Test
        @DisplayName("roomId가 없을 때 400 error")
        void missing_roomId() throws Exception {
            Map<String, Object> req = buildValidRequest();
            req.remove("roomId");
            mockMvc.perform(get("/rooms//users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                            .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("DB에 존재하지 않는 roomId가 들어오면 404 error")
        void not_found_roomId() throws Exception {
            Map<String, Object> req = buildValidRequest();
            req.remove("roomId");
            req.put("roomId", 99999L);
            assertRoomNotFound(req, "존재하지 않는 ROOM 입니다.");
        }
    }
}
