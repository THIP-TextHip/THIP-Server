package konkuk.thip.feed.adapter.in.web;

import com.jayway.jsonpath.JsonPath;
import konkuk.thip.common.util.EnumMappings;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.config.TestS3MockConfig;
import konkuk.thip.feed.domain.Tag;
import konkuk.thip.room.domain.Category;
import konkuk.thip.user.domain.Alias;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static konkuk.thip.feed.domain.Tag.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@Import(TestS3MockConfig.class)
@DisplayName("[통합] 피드 작성을 위한 화면 조회 api 통합 테스트")
class FeedShowWriteInfoApiTest {

    @Autowired
    private MockMvc mockMvc;

    private Alias literatureAlias;
    private Alias scienceAlias;
    private Category literatureCategory;
    private Category scienceCategory;

    @BeforeEach
    void setUp() {
        literatureAlias = TestEntityFactory.createLiteratureAlias();
        scienceAlias = TestEntityFactory.createScienceAlias();
    }

    @Test
    @DisplayName("피드 작성을 위한 화면을 조회하면, enum으로 정의된 Category/Tag 매핑이 형식과 내용 모두 정확히 반환된다.")
    void showFeedWriteInfo_returnsCategoryAndTags() throws Exception {
        // given: SSOT인 EnumMappings에서 기대 맵을 구성
        Map<Category, java.util.List<Tag>> categoryToTags = EnumMappings.getCategoryToTags();
        int expectedCategoryCount = categoryToTags.size();

        // when
        var mvcResult = mockMvc.perform(get("/feeds/write-info")
                        .contentType(MediaType.APPLICATION_JSON))
                // 1차: 상태 코드 및 카테고리 개수 검증(하드코딩 지양)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.categoryList", hasSize(expectedCategoryCount)))
                .andReturn();

        // then: 응답을 파싱하여 형식과 내용 동일성 검증
        String body = mvcResult.getResponse().getContentAsString();

        // 스키마 형태 검증: 각 요소에 category(String), tagList(Array) 존재
        JsonPath.read(body, "$.data.categoryList[*].category");
        JsonPath.read(body, "$.data.categoryList[*].tagList");

        // 실제 응답: category(String) -> tag(Set<String>)
        java.util.List<java.util.Map<String, Object>> actualList =
                JsonPath.read(body, "$.data.categoryList");

        Map<String, Set<String>> actualMap = new HashMap<>();
        for (Map<String, Object> item : actualList) {
            String categoryName = (String) item.get("category");
            @SuppressWarnings("unchecked")
            List<String> tags = (List<String>) item.get("tagList");
            actualMap.put(categoryName, new HashSet<>(tags));
        }

        // 기대값: Category.getValue() -> Set<Tag.getValue()>
        Map<String, Set<String>> expectedMap = new HashMap<>();
        for (var entry : categoryToTags.entrySet()) {
            String categoryName = entry.getKey().getValue();
            Set<String> tagValues = entry.getValue().stream()
                    .map(konkuk.thip.feed.domain.Tag::getValue)
                    .collect(Collectors.toSet());
            expectedMap.put(categoryName, tagValues);
        }

        // 키(카테고리) 동일성
        assertThat(
                "카테고리 키가 EnumMappings과 일치하지 않습니다.",
                actualMap.keySet(), org.hamcrest.Matchers.equalTo(expectedMap.keySet())
        );

        // 각 카테고리의 태그 집합 동등성
        for (String key : expectedMap.keySet()) {
            assertThat(
                    "태그 목록이 일치하지 않습니다. category=" + key,
                    actualMap.get(key),
                    org.hamcrest.Matchers.equalTo(expectedMap.get(key))
            );
        }
    }

}
