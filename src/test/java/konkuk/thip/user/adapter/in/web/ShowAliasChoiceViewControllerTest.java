package konkuk.thip.user.adapter.in.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import konkuk.thip.room.adapter.out.jpa.CategoryJpaEntity;
import konkuk.thip.room.adapter.out.persistence.CategoryJpaRepository;
import konkuk.thip.user.adapter.in.web.response.ShowAliasChoiceResponse;
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
@AutoConfigureMockMvc
class ShowAliasChoiceViewControllerTest {

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
        ShowAliasChoiceResponse showResponse = objectMapper.treeToValue(jsonNode.get("data"), ShowAliasChoiceResponse.class);
        List<ShowAliasChoiceResponse.AliasChoice> choices = showResponse.aliasChoices();

        assertThat(choices).hasSize(2);
        assertThat(choices)
                .extracting("aliasName", "categoryName", "imageUrl", "color")
                .containsExactlyInAnyOrder(
                        tuple("문학가", "문학", "문학가_image", "red"),
                        tuple("과학자", "과학/IT", "과학자_image", "blue")
                );
    }

    private void saveAliasesAndCategories() {
        AliasJpaEntity alias1 = AliasJpaEntity.builder()
                .value("문학가")
                .imageUrl("문학가_image")
                .color("red")
                .build();
        aliasJpaRepository.save(alias1);

        CategoryJpaEntity category1 = CategoryJpaEntity.builder()
                .value("문학")
                .aliasForCategoryJpaEntity(alias1)
                .build();
        categoryJpaRepository.save(category1);

        AliasJpaEntity alias2 = AliasJpaEntity.builder()
                .value("과학자")
                .imageUrl("과학자_image")
                .color("blue")
                .build();
        aliasJpaRepository.save(alias2);

        CategoryJpaEntity category2 = CategoryJpaEntity.builder()
                .value("과학/IT")
                .aliasForCategoryJpaEntity(alias2)
                .build();
        categoryJpaRepository.save(category2);
    }
}
