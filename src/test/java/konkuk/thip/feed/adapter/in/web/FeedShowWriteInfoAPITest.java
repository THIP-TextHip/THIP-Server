package konkuk.thip.feed.adapter.in.web;


import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.config.TestS3MockConfig;
import konkuk.thip.feed.adapter.out.persistence.repository.Tag.TagJpaRepository;
import konkuk.thip.room.adapter.out.jpa.CategoryJpaEntity;
import konkuk.thip.room.adapter.out.persistence.repository.category.CategoryJpaRepository;
import konkuk.thip.user.adapter.out.jpa.AliasJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.alias.AliasJpaRepository;
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

import static konkuk.thip.feed.domain.Tag.*;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@Import(TestS3MockConfig.class)
@DisplayName("[통합] 피드 작성을 위한 화면 조회 api 통합 테스트")
class FeedShowWriteInfoAPITest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired private CategoryJpaRepository categoryJpaRepository;
    @Autowired private TagJpaRepository tagJpaRepository;
    @Autowired private AliasJpaRepository aliasJpaRepository;

    private AliasJpaEntity literatureAlias;
    private AliasJpaEntity scienceAlias;
    private CategoryJpaEntity literatureCategory;
    private CategoryJpaEntity scienceCategory;

    @BeforeEach
    void setUp() {
        literatureAlias = aliasJpaRepository.save(TestEntityFactory.createLiteratureAlias());
        scienceAlias = aliasJpaRepository.save(TestEntityFactory.createScienceAlias());
    }

    @Test
    @DisplayName("피드 작성을 위한 화면을 조회하면, DB의 카테고리별 하위 태그 리스트가 전체 반환된다.")
    void showFeedWriteInfo_returnsCategoryAndTags() throws Exception {

        // given
        literatureCategory = categoryJpaRepository.save(TestEntityFactory.createLiteratureCategory(literatureAlias));
        scienceCategory = categoryJpaRepository.save(TestEntityFactory.createScienceCategory(scienceAlias));
        tagJpaRepository.save(TestEntityFactory.createTag(literatureCategory,KOREAN_NOVEL.getValue()));
        tagJpaRepository.save(TestEntityFactory.createTag(literatureCategory,FOREIGN_NOVEL.getValue()));
        tagJpaRepository.save(TestEntityFactory.createTag(literatureCategory,CLASSIC_LITERATURE.getValue()));
        tagJpaRepository.save(TestEntityFactory.createTag(scienceCategory,GENERAL_SCIENCE.getValue()));
        tagJpaRepository.save(TestEntityFactory.createTag(scienceCategory,PHYSICS.getValue()));
        tagJpaRepository.save(TestEntityFactory.createTag( scienceCategory,CHEMISTRY.getValue()));

        // when // then
        mockMvc.perform(get("/feeds/write-info")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.categoryList", hasSize(2)))
                .andExpect(jsonPath("$.data.categoryList[0].category", is(literatureCategory.getValue())))
                .andExpect(jsonPath("$.data.categoryList[0].tagList[0]", is(KOREAN_NOVEL.getValue())))
                .andExpect(jsonPath("$.data.categoryList[0].tagList[1]", is(FOREIGN_NOVEL.getValue())))
                .andExpect(jsonPath("$.data.categoryList[0].tagList[2]", is(CLASSIC_LITERATURE.getValue())))
                .andExpect(jsonPath("$.data.categoryList[1].category", is(scienceCategory.getValue())))
                .andExpect(jsonPath("$.data.categoryList[1].tagList[0]", is(GENERAL_SCIENCE.getValue())))
                .andExpect(jsonPath("$.data.categoryList[1].tagList[1]", is(PHYSICS.getValue())))
                .andExpect(jsonPath("$.data.categoryList[1].tagList[2]", is(CHEMISTRY.getValue())));
    }

}
