package konkuk.thip.user.adapter.in.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.room.adapter.out.jpa.CategoryJpaEntity;
import konkuk.thip.room.adapter.out.persistence.repository.CategoryJpaRepository;
import konkuk.thip.user.adapter.in.web.response.UserViewAliasChoiceResponse;
import konkuk.thip.user.adapter.out.jpa.AliasJpaEntity;
import konkuk.thip.user.adapter.out.persistence.AliasJpaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

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

    @Autowired
    private AliasJpaRepository aliasJpaRepository;

    @Autowired
    private CategoryJpaRepository categoryJpaRepository;

    @AfterEach
    void tearDown() {
        categoryJpaRepository.deleteAll();
        aliasJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("현재 DB에 존재하는 모든 [칭호, 카테고리] 정보를 반환한다.")
    void show_alias_choice_view() throws Exception {
        //given
        saveAliasesAndCategories();

        //when
        ResultActions result = mockMvc.perform(get("/users/alias")
                .contentType(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.aliasChoices").exists());

        String json = result.andReturn().getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(json);
        UserViewAliasChoiceResponse showResponse = objectMapper.treeToValue(jsonNode.get("data"), UserViewAliasChoiceResponse.class);
        List<UserViewAliasChoiceResponse.AliasChoice> choices = showResponse.aliasChoices();

        assertThat(choices).hasSize(2);
        assertThat(choices)
                .extracting("aliasName", "categoryName", "imageUrl", "color")
                .containsExactlyInAnyOrder(
                        tuple("문학가", "문학", "문학_image", "문학_color"),
                        tuple("과학자", "과학/IT", "과학_image", "과학_color")
                );
    }

    private void saveAliasesAndCategories() {
        AliasJpaEntity alias1 = aliasJpaRepository.save(TestEntityFactory.createLiteratureAlias());
        CategoryJpaEntity category1 = categoryJpaRepository.save(TestEntityFactory.createLiteratureCategory(alias1));

        AliasJpaEntity alias2 = aliasJpaRepository.save(TestEntityFactory.createScienceAlias());
        CategoryJpaEntity category2 = categoryJpaRepository.save(TestEntityFactory.createScienceCategory(alias2));
    }
}
