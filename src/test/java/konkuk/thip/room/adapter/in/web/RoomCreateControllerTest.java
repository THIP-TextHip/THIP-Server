package konkuk.thip.room.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("[단위] 방 생성 api controller 단위 테스트")
class RoomCreateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;


    private Map<String, Object> buildValidRequest() {
        Map<String, Object> request = new HashMap<>();
        request.put("isbn", "9788954682152");
        request.put("category", "소설");
        request.put("roomName", "방이름");
        request.put("description", "방설명");
        request.put("progressStartDate", "2025.07.10");
        request.put("progressEndDate", "2025.08.10");
        request.put("recruitCount", 3);
        request.put("password", null);
        request.put("isPublic", true);
        return request;
    }

    private void assertBad(Map<String, Object> req, String msg) throws Exception {
        mockMvc.perform(post("/rooms")
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(API_INVALID_PARAM.getCode()))
                .andExpect(jsonPath("$.message", containsString(msg)));
    }

    @Nested
    @DisplayName("ISBN 검증")
    class IsbnValidation {
        @Test
        @DisplayName("빈 문자열일 때 400 error")
        void blank_isbn() throws Exception {
            Map<String, Object> req = buildValidRequest();
            req.put("isbn", "");
            assertBad(req, "ISBN은 필수입니다.");
        }
    }

    @Nested
    @DisplayName("Category 검증")
    class CategoryValidation {
        @Test
        @DisplayName("빈 문자열일 때 400 error")
        void blank_category() throws Exception {
            Map<String, Object> req = buildValidRequest();
            req.put("category", "");
            assertBad(req, "카테고리는 필수입니다.");
        }
    }

    @Nested
    @DisplayName("RoomName 검증")
    class RoomNameValidation {
        @Test
        @DisplayName("빈 문자열일 때 400 error")
        void blank_room_name() throws Exception {
            Map<String, Object> req = buildValidRequest();
            req.put("roomName", "");
            assertBad(req, "방 이름은 필수입니다.");
        }
    }

    @Nested
    @DisplayName("Description 검증")
    class DescriptionValidation {
        @Test
        @DisplayName("빈 문자열일 때 400 error")
        void blank_description() throws Exception {
            Map<String, Object> req = buildValidRequest();
            req.put("description", "");
            assertBad(req, "설명은 필수입니다.");
        }
    }

    @Nested
    @DisplayName("StartDate 검증")
    class StartDateValidation {
        @Test
        @DisplayName("빈 문자열일 때 400 error")
        void blank_start_date() throws Exception {
            Map<String, Object> req = buildValidRequest();
            req.put("progressStartDate", "");
            assertBad(req, "진행 시작일은 yyyy.MM.dd 형식이어야 합니다.");
        }
        @Test
        @DisplayName("형식 벗어날 때 400 error")
        void pattern_start_date() throws Exception {
            Map<String, Object> req = buildValidRequest();
            req.put("progressStartDate", "2025-07-10");
            assertBad(req, "진행 시작일은 yyyy.MM.dd 형식이어야 합니다.");
        }
    }

    @Nested
    @DisplayName("EndDate 검증")
    class EndDateValidation {
        @Test
        @DisplayName("빈 문자열일 때 400 error")
        void blank_end_date() throws Exception {
            Map<String, Object> req = buildValidRequest();
            req.put("progressEndDate", "");
            assertBad(req, "진행 종료일은 yyyy.MM.dd 형식이어야 합니다.");
        }
        @Test
        @DisplayName("형식 벗어날 때 400 error")
        void pattern_end_date() throws Exception {
            Map<String, Object> req = buildValidRequest();
            req.put("progressEndDate", "2025/08/10");
            assertBad(req, "진행 종료일은 yyyy.MM.dd 형식이어야 합니다.");
        }
    }

    @Nested
    @DisplayName("RecruitCount 검증")
    class RecruitCountValidation {
        @Test
        @DisplayName("1 미만일 때 400 error")
        void less_than_one() throws Exception {
            Map<String, Object> req = buildValidRequest();
            req.put("recruitCount", 0);
            assertBad(req, "모집 인원은 최소 1명이어야 합니다.");
        }

        @Test
        @DisplayName("30 초과일 때 400 error")
        void greater_than_max() throws Exception {
            Map<String, Object> req = buildValidRequest();
            req.put("recruitCount", 31);
            assertBad(req, "모집 인원은 최대 30명이어야 합니다.");
        }
    }

    @Nested
    @DisplayName("Password 검증")
    class PasswordValidation {
        @Test
        @DisplayName("숫자로 구성되지 않았을 때 400 error")
        void invalid_password() throws Exception {
            Map<String, Object> req = buildValidRequest();
            req.put("password", "12ab");
            assertBad(req, "비밀번호는 숫자 4자리여야 합니다.");
        }

        @Test
        @DisplayName("4자리 숫자 아닐 때 400 error")
        void short_password() throws Exception {
            Map<String, Object> req = buildValidRequest();
            req.put("password", "123");
            assertBad(req, "비밀번호는 숫자 4자리여야 합니다.");
        }
    }

    @Nested
    @DisplayName("isPublic 검증")
    class IsPublicValidation {
        @Test
        @DisplayName("값이 없을 때 400 error")
        void missing_is_public() throws Exception {
            Map<String, Object> req = buildValidRequest();
            req.put("isPublic", null);
            assertBad(req, "방 공개 설정 여부는 필수입니다.");
        }
    }
}
