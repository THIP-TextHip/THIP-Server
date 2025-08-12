package konkuk.thip.room.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import konkuk.thip.room.adapter.in.web.request.AttendanceCheckCreateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static konkuk.thip.common.exception.code.ErrorCode.API_INVALID_PARAM;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("[단위] 오늘의 한마디 생성 api controller 단위 테스트")
class AttendanceCheckCreateControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Test
    @DisplayName("오늘의 한마디 작성 요청 시 request body의 content 값이 공백일 경우, 400 에러가 발생한다.")
    void attendance_check_fail_content_blank() throws Exception {
        //given
        AttendanceCheckCreateRequest request = new AttendanceCheckCreateRequest("");

        //when //then
        mockMvc.perform(post("/rooms/{roomId}/daily-greeting", 1L)
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(API_INVALID_PARAM.getCode()))
                .andExpect(jsonPath("$.message", containsString("오늘의 한마디 내용은 필수입니다.")));
    }
}
