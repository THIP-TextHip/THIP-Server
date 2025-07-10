package konkuk.thip.user.adapter.in.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import konkuk.thip.common.security.util.JwtUtil;
import konkuk.thip.user.adapter.in.web.request.UserSignupRequest;
import konkuk.thip.user.adapter.out.jpa.AliasJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.AliasJpaRepository;
import konkuk.thip.user.adapter.out.persistence.UserJpaRepository;
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

import static konkuk.thip.common.exception.code.ErrorCode.API_INVALID_PARAM;
import static konkuk.thip.common.exception.code.ErrorCode.AUTH_TOKEN_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@DisplayName("[통합] UserSignupController 테스트")
class UserSignupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AliasJpaRepository aliasJpaRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @AfterEach
    void tearDown() {
        userJpaRepository.deleteAll();
        aliasJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("[칭호id, 닉네임] 정보를 바탕으로 회원가입을 진행한다.")
    void signup_success() throws Exception {
        //given : alias 생성, 회원가입 request 생성
        AliasJpaEntity aliasJpaEntity = AliasJpaEntity.builder()
                .value("문학가")
                .color("문학_color")
                .imageUrl("문학_image")
                .build();
        aliasJpaRepository.save(aliasJpaEntity);

        UserSignupRequest request = new UserSignupRequest(
                aliasJpaEntity.getValue(),
                "테스트유저"
        );

        //when : 회원가입 api 호출 + 임시 토큰 발급
        String testToken = jwtUtil.createSignupToken("kakao_12345678");
        ResultActions result = mockMvc.perform(post("/users/signup")
                .header("Authorization", "Bearer " + testToken)  //헤더 추가
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").exists());

        String json = result.andReturn().getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(json);
        Long userId = jsonNode.path("data").path("userId").asLong();

        UserJpaEntity userJpaEntity = userJpaRepository.findById(userId).orElse(null);
        AliasJpaEntity userAliasJpaEntity = aliasJpaRepository.findByValue(request.aliasName()).orElse(null);

        assertThat(userAliasJpaEntity.getValue()).isEqualTo(request.aliasName());
        assertThat(userJpaEntity.getNickname()).isEqualTo(request.nickname());
    }

    @Test
    @DisplayName("[칭호id]값이 null일 경우, 400 error가 발생한다.")
    void signup_alias_id_null() throws Exception {
        //given: aliasId null
        UserSignupRequest request = new UserSignupRequest(
                null,
                "테스트유저"
        );

        //when //then
        String testToken = jwtUtil.createSignupToken("kakao_12345678");
        mockMvc.perform(post("/users/signup")
                .header("Authorization", "Bearer " + testToken)  //헤더 추가
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(API_INVALID_PARAM.getCode()))
                .andExpect(jsonPath("$.message", containsString("aliasName은 필수입니다.")));
    }

    @Test
    @DisplayName("[닉네임]값이 공백일 경우, 400 error가 발생한다.")
    void signup_nickname_blank() throws Exception {
        //given: nickname blank
        UserSignupRequest request = new UserSignupRequest(
                "문학가",
                ""
        );

        //when //then
        String testToken = jwtUtil.createSignupToken("kakao_12345678");
        mockMvc.perform(post("/users/signup")
                .header("Authorization", "Bearer " + testToken)  //헤더 추가
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
                "문학가",
                "닉네임!!"
        );

        //when //then
        String testToken = jwtUtil.createSignupToken("kakao_12345678");
        mockMvc.perform(post("/users/signup")
                        .header("Authorization", "Bearer " + testToken)  //헤더 추가
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
                "문학가",
                "11글자닉네임입니다아"
        );

        //when //then
        String testToken = jwtUtil.createSignupToken("kakao_12345678");
        mockMvc.perform(post("/users/signup")
                .header("Authorization", "Bearer " + testToken)  //헤더 추가
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(API_INVALID_PARAM.getCode()))
                .andExpect(jsonPath("$.message", containsString("닉네임은 최대 10자 입니다.")));
    }

    @Test
    @DisplayName("임시 토큰을 통해 @Oauth2Id로 oauth2Id를 정확히 추출하여 회원가입에 성공한다.")
    void signup_whenValidSignupToken_thenExtractOauth2IdCorrectly() throws Exception {
        //given : alias 데이터 저장
        AliasJpaEntity aliasJpaEntity = AliasJpaEntity.builder()
                .value("문학가")
                .color("문학_color")
                .imageUrl("문학_image")
                .build();
        aliasJpaRepository.save(aliasJpaEntity);

        //회원가입 request 생성
        UserSignupRequest request = new UserSignupRequest(
                aliasJpaEntity.getValue(),
                "테스트유저"
        );

        //when : 임시 토큰 생성
        String expectedOauth2Id = "kakao_12345678";
        String testToken = jwtUtil.createSignupToken(expectedOauth2Id);

        //when : 회원가입 API 호출
        ResultActions result = mockMvc.perform(post("/users/signup")
                .header("Authorization", "Bearer " + testToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        //then : 응답 검증
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").exists());

        //등록된 사용자 oauth2Id 검증
        String responseBody = result.andReturn().getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        Long savedUserId = jsonNode.path("data").path("userId").asLong();

        UserJpaEntity savedUser = userJpaRepository.findById(savedUserId).orElseThrow();

        assertThat(savedUser.getOauth2Id()).isEqualTo(expectedOauth2Id);
        assertThat(savedUser.getNickname()).isEqualTo("테스트유저");
    }

    @Test
    @DisplayName("헤더에 토큰을 넣지 않고 요청시에 401 error가 발생한다.")
    void signup_whenNoToken_thenUnauthorized() throws Exception {
        //given: aliasId null
        UserSignupRequest request = new UserSignupRequest(
                "문학가",
                "테스트유저"
        );

        //when //then
        mockMvc.perform(post("/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(AUTH_TOKEN_NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message", containsString(AUTH_TOKEN_NOT_FOUND.getMessage())));
    }


}
