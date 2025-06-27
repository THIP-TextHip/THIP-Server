package konkuk.thip.user.adapter.in.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import konkuk.thip.user.adapter.in.web.request.UserSignupRequest;
import konkuk.thip.user.adapter.out.jpa.AliasJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.AliasJpaRepository;
import konkuk.thip.user.adapter.out.persistence.UserJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static konkuk.thip.common.exception.code.ErrorCode.API_INVALID_PARAM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class UserSignupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AliasJpaRepository aliasJpaRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @BeforeEach
    void tearDown() {
        userJpaRepository.deleteAll();
        aliasJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("[칭호id, 닉네임, 이메일] 정보를 바탕으로 회원가입을 진행한다.")
    void signup_success() throws Exception {
        //given : alias 생성, 회원가입 request 생성
        AliasJpaEntity aliasJpaEntity = AliasJpaEntity.builder()
                .value("칭호")
                .color("blue")
                .imageUrl("http://image.url")
                .build();
        aliasJpaRepository.save(aliasJpaEntity);

        UserSignupRequest request = new UserSignupRequest(
                aliasJpaEntity.getAliasId(),
                "테스트유저",
                "test@test.com"
        );

        //when : 회원가입 api 호출
        ResultActions result = mockMvc.perform(post("/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").exists());

        String json = result.andReturn().getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(json);
        Long userId = jsonNode.path("data").path("userId").asLong();

        UserJpaEntity userJpaEntity = userJpaRepository.findById(userId).orElse(null);

        assertThat(userJpaEntity.getAliasForUserJpaEntity().getAliasId()).isEqualTo(request.aliasId());
        assertThat(userJpaEntity.getNickname()).isEqualTo(request.nickname());
        assertThat(userJpaEntity.getEmail()).isEqualTo(request.email());
    }

    @Test
    @DisplayName("[칭호id]값이 null일 경우, 400 error가 발생한다.")
    void signup_alias_id_null() throws Exception {
        //given: aliasId null
        UserSignupRequest request = new UserSignupRequest(
                null,
                "테스트유저",
                "test@test.com"
        );

        //when //then
        mockMvc.perform(post("/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(API_INVALID_PARAM.getCode()))
                .andExpect(jsonPath("$.message", containsString("aliasId는 필수입니다.")));
    }

    @Test
    @DisplayName("[닉네임]값이 공백일 경우, 400 error가 발생한다.")
    void signup_nickname_blank() throws Exception {
        //given: nickname blank
        UserSignupRequest request = new UserSignupRequest(
                1L,
                "",
                "test@test.com"
        );

        //when //then
        mockMvc.perform(post("/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(API_INVALID_PARAM.getCode()))
                .andExpect(jsonPath("$.message", containsString("닉네임은 한글, 영어, 숫자로만 구성되어야 합니다.(공백불가)")));
    }

    @Test
    @DisplayName("[닉네임]값이 한글, 영어, 숫자 외의 문자를 포함할 경우, 400 error가 발생한다.")
    void signup_nickname_invalid_pattern() throws Exception {
        //given: nickname with invalid characters
        UserSignupRequest request = new UserSignupRequest(
                1L,
                "닉네임!!",
                "test@test.com"
        );

        //when //then
        mockMvc.perform(post("/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(API_INVALID_PARAM.getCode()))
                .andExpect(jsonPath("$.message", containsString("닉네임은 한글, 영어, 숫자로만 구성되어야 합니다.(공백불가)")));
    }

    @Test
    @DisplayName("[닉네임]값이 11자 이상일 경우, 400 error가 발생한다.")
    void signup_nickname_too_long() throws Exception {
        //given: 11글자 nickname
        UserSignupRequest request = new UserSignupRequest(
                1L,
                "11글자닉네임입니다아",
                "test@test.com"
        );

        //when //then
        mockMvc.perform(post("/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(API_INVALID_PARAM.getCode()))
                .andExpect(jsonPath("$.message", containsString("닉네임은 최대 10자 입니다.")));
    }

    @Test
    @DisplayName("[이메일]값이 공백일 경우, 400 error가 발생한다.")
    void signup_email_blank() throws Exception {
        //given
        UserSignupRequest request = new UserSignupRequest(
                1L,
                "테스트유저",
                ""
        );

        //when //then
        mockMvc.perform(post("/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(API_INVALID_PARAM.getCode()))
                .andExpect(jsonPath("$.message", containsString("이메일은 공백일 수 없습니다.")));
    }

    @Test
    @DisplayName("[이메일]값이 유효한 이메일 형식이 아닐 경우, 400 error가 발생한다.")
    void signup_email_invalid_format() throws Exception {
        //given
        UserSignupRequest request = new UserSignupRequest(
                1L,
                "테스트유저",
                "invalid-email-format"
        );

        //when //then
        mockMvc.perform(post("/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("40002"))
                .andExpect(jsonPath("$.message", containsString("이메일 형식이 올바르지 않습니다.")));
    }
}
