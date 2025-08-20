package konkuk.thip.feed.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import konkuk.thip.feed.domain.value.Tag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static konkuk.thip.common.exception.code.ErrorCode.*;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("[단위] 피드 생성 api controller 단위 테스트")
class FeedCreateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Map<String, Object> buildValidRequest() {
        Map<String, Object> request = new HashMap<>();
        request.put("isbn", "9788954682152");
        request.put("contentBody", "테스트 콘텐츠");
        request.put("isPublic", true);
        request.put("category", "문학");
        request.put("tagList", List.of(Tag.PHYSICS.getValue(), Tag.CHEMISTRY.getValue()));
        return request;
    }

    private void assertBadRequest_InvalidFeedCreate(Map<String, Object> request, String message) throws Exception {
        mockMvc.perform(multipart("/feeds")
                        .file(new MockMultipartFile(
                                "request", "", MediaType.APPLICATION_JSON_VALUE,
                                objectMapper.writeValueAsBytes(request)))
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                        .requestAttr("userId", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INVALID_FEED_COMMAND.getCode()))
                .andExpect(jsonPath("$.message", containsString(message)));
    }

    private void assertBadRequest_InvalidParam(Map<String, Object> request, String message) throws Exception {
        mockMvc.perform(multipart("/feeds")
                        .file(new MockMultipartFile(
                                "request", "", MediaType.APPLICATION_JSON_VALUE,
                                objectMapper.writeValueAsBytes(request)))
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                        .requestAttr("userId", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(API_INVALID_PARAM.getCode()))
                .andExpect(jsonPath("$.message", containsString(message)));
    }

    @Nested
    @DisplayName("기본 필드 검증")
    class BasicValidation {

        @Test
        @DisplayName("ISBN이 빈 문자열이면 400 반환")
        void blankIsbn() throws Exception {
            Map<String, Object> req = buildValidRequest();
            req.put("isbn", "");
            assertBadRequest_InvalidParam(req, "ISBN은 필수입니다.");
        }

        @Test
        @DisplayName("콘텐츠 내용이 없으면 400 반환")
        void blankContent() throws Exception {
            Map<String, Object> req = buildValidRequest();
            req.put("contentBody", "");
            assertBadRequest_InvalidParam(req, "콘텐츠 내용은 필수입니다.");
        }

        @Test
        @DisplayName("값이 없을 때 400 error")
        void missing_is_public() throws Exception {
            Map<String, Object> req = buildValidRequest();
            req.put("isPublic", null);
            assertBadRequest_InvalidParam(req, "방 공개 설정 여부는 필수입니다.");
        }

    }

    @Nested
    @DisplayName("태그 입력 불일치 검증")
    class TagValidation {

        @Test
        @DisplayName("태그가 6개 이상이면 400 반환")
        void tooManyTags() throws Exception {
            Map<String, Object> req = buildValidRequest();
            req.put("tagList", List.of(Tag.PHYSICS.getValue(), Tag.CHEMISTRY.getValue(), Tag.BIOLOGY.getValue(),
                    Tag.ARCHITECTURE.getValue(), Tag.ARCHITECTURE.getValue(), Tag.DANCE.getValue()));
            assertBadRequest_InvalidFeedCreate(req, "태그는 최대 5개까지 입력할 수 있습니다.");
        }

        @Test
        @DisplayName("태그가 중복되면 400 반환")
        void duplicatedTags() throws Exception {
            Map<String, Object> req = buildValidRequest();
            req.put("tagList", List.of(Tag.PHYSICS.getValue(), Tag.PHYSICS.getValue()));
            assertBadRequest_InvalidFeedCreate(req, TAG_SHOULD_BE_UNIQUE.getMessage());
        }
    }

    @Nested
    @DisplayName("이미지 개수 검증")
    class ImageValidation {

        @Test
        @DisplayName("이미지가 3개 초과되면 400 반환")
        void tooManyImages() throws Exception {
            Map<String, Object> req = buildValidRequest();
            MockMultipartFile requestPart = new MockMultipartFile(
                    "request", "", MediaType.APPLICATION_JSON_VALUE,
                    objectMapper.writeValueAsBytes(req)
            );

            // 이미지 4개 세팅
            List<MockMultipartFile> images = List.of(
                    new MockMultipartFile("images", "img1.jpg", MediaType.IMAGE_JPEG_VALUE, "1".getBytes()),
                    new MockMultipartFile("images", "img2.jpg", MediaType.IMAGE_JPEG_VALUE, "2".getBytes()),
                    new MockMultipartFile("images", "img3.jpg", MediaType.IMAGE_JPEG_VALUE, "3".getBytes()),
                    new MockMultipartFile("images", "img4.jpg", MediaType.IMAGE_JPEG_VALUE, "4".getBytes())
            );

            ResultActions result = mockMvc.perform(multipart("/feeds")
                    .file(requestPart)
                    .file(images.get(0))
                    .file(images.get(1))
                    .file(images.get(2))
                    .file(images.get(3))
                    .requestAttr("userId", 1L)
                    .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
            );

            result.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(CONTENT_LIST_SIZE_OVERFLOW.getCode()))
                    .andExpect(jsonPath("$.message",containsString(CONTENT_LIST_SIZE_OVERFLOW.getMessage())));

        }
    }
}
