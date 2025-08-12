package konkuk.thip.user.adapter.in.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.user.adapter.in.web.request.UserVerifyNicknameRequest;
import konkuk.thip.user.adapter.out.jpa.AliasJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.alias.AliasJpaRepository;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
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

import java.time.LocalDate;

import static konkuk.thip.common.exception.code.ErrorCode.API_INVALID_PARAM;
import static konkuk.thip.user.adapter.out.jpa.UserRole.USER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("[통합] 닉네임 중복 검증 api 테스트")
class UserVerifyNicknameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private AliasJpaRepository aliasJpaRepository;

    @AfterEach
    void tearDown() {
        userJpaRepository.deleteAll();
        aliasJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("[닉네임]값이 unique 할 경우, true를 반환한다.")
    void verify_nickname_true() throws Exception {
        //given
        UserVerifyNicknameRequest request = new UserVerifyNicknameRequest("테스트유저");

        //when
        ResultActions result = mockMvc.perform(post("/users/nickname")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isVerified").exists());

        String json = result.andReturn().getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(json);
        boolean isVerified = jsonNode.path("data").path("isVerified").asBoolean();

        assertThat(isVerified).isTrue();
    }

    @Test
    @DisplayName("[닉네임]값이 이미 DB에 존재하는 경우, false를 반환한다.")
    void verify_nickname_false() throws Exception {
        //given: DB에 "테스트유저" 생성
        AliasJpaEntity aliasJpaEntity = aliasJpaRepository.save(TestEntityFactory.createLiteratureAlias());

        UserJpaEntity userJpaEntity = UserJpaEntity.builder()
                .nickname("테스트유저")
                .nicknameUpdatedAt(LocalDate.now().minusMonths(7))
                .role(USER)
                .oauth2Id("kakao_12345678")
                .aliasForUserJpaEntity(aliasJpaEntity)
                .build();
        userJpaRepository.save(userJpaEntity);

        UserVerifyNicknameRequest request = new UserVerifyNicknameRequest("테스트유저");

        //when
        ResultActions result = mockMvc.perform(post("/users/nickname")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isVerified").exists());

        String json = result.andReturn().getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(json);
        boolean isVerified = jsonNode.path("data").path("isVerified").asBoolean();

        assertThat(isVerified).isFalse();
    }

    @Test
    @DisplayName("[닉네임]값이 공백일 경우, 400 error가 발생한다.")
    void nickname_blank() throws Exception {
        //given: nickname blank
        UserVerifyNicknameRequest request = new UserVerifyNicknameRequest("");

        //when //then
        mockMvc.perform(post("/users/nickname")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(API_INVALID_PARAM.getCode()))
                .andExpect(jsonPath("$.message", containsString("닉네임은 한글, 영어, 숫자로만 구성되어야 합니다.(공백불가)")));
    }

    @Test
    @DisplayName("[닉네임]값이 한글, 영어, 숫자 외의 문자를 포함할 경우, 400 error가 발생한다.")
    void nickname_invalid_pattern() throws Exception {
        //given: nickname with invalid characters
        UserVerifyNicknameRequest request = new UserVerifyNicknameRequest("닉네임!!");

        //when //then
        mockMvc.perform(post("/users/nickname")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(API_INVALID_PARAM.getCode()))
                .andExpect(jsonPath("$.message", containsString("닉네임은 한글, 영어, 숫자로만 구성되어야 합니다.(공백불가)")));
    }

    @Test
    @DisplayName("[닉네임]값이 11자 이상일 경우, 400 error가 발생한다.")
    void nickname_too_long() throws Exception {
        //given: 11글자 nickname
        UserVerifyNicknameRequest request = new UserVerifyNicknameRequest("11글자닉네임입니다아");

        //when //then
        mockMvc.perform(post("/users/nickname")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(API_INVALID_PARAM.getCode()))
                .andExpect(jsonPath("$.message", containsString("닉네임은 최대 10자 입니다.")));
    }
}
