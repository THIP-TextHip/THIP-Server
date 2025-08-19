package konkuk.thip.user.adapter.in.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import konkuk.thip.common.util.EnumMappings;
import konkuk.thip.room.domain.Category;
import konkuk.thip.user.domain.Alias;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("[통합] 사용자 칭호 선택 api 테스트")
class UserViewAliasChoiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("유저 칭호 선택 화면을 조회하면, enum으로 정의된 Alias/Category 매핑이 형식과 내용 모두 정확히 반환된다.")
    void show_alias_choice_view() throws Exception {
        // given: EnumMappings을 기준으로 기대값 구성
        var aliasToCategory = EnumMappings.getAliasToCategory();
        int expectedCount = aliasToCategory.size();

        // when
        var mvcResult = mockMvc.perform(get("/users/alias")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.aliasChoices").isArray())
                .andExpect(jsonPath("$.data.aliasChoices.length()").value(expectedCount))
                .andReturn();

        // then: 응답 파싱 후 형식 및 내용 검증
        String body = mvcResult.getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(body);
        JsonNode choices = root.path("data").path("aliasChoices");
        assertThat(choices.isArray()).isTrue();
        assertThat(choices.size()).isEqualTo(expectedCount);

        // 실제 응답을 (aliasName, categoryName, imageUrl, aliasColor) 튜플로 수집
        var actualTuples = new ArrayList<Tuple>();
        for (JsonNode n : choices) {
            String aliasName = n.path("aliasName").asText();
            String categoryName = n.path("categoryName").asText();
            String imageUrl = n.path("imageUrl").asText();
            String aliasColor = n.path("aliasColor").asText();

            // 스키마 기본 검증
            assertThat(aliasName).isNotBlank();
            assertThat(categoryName).isNotBlank();
            assertThat(imageUrl).isNotBlank();
            assertThat(aliasColor).isNotBlank();

            actualTuples.add(tuple(aliasName, categoryName, imageUrl, aliasColor));
        }

        // 기대 튜플 구성: EnumMappings의 SSOT 기준
        var expectedTuples = aliasToCategory.entrySet().stream()
                .map(e -> {
                    Alias alias = e.getKey();
                    Category category = e.getValue();
                    return tuple(
                            alias.getValue(),      // aliasName
                            category.getValue(),   // categoryName
                            alias.getImageUrl(),   // imageUrl
                            alias.getColor()       // aliasColor
                    );
                })
                .toList();

        assertThat(actualTuples)
                .containsExactlyInAnyOrderElementsOf(expectedTuples);
    }
}
