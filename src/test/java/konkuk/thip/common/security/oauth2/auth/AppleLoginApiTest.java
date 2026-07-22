package konkuk.thip.common.security.oauth2.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import konkuk.thip.common.security.oauth2.apple.AppleIdentityTokenVerifier;
import konkuk.thip.common.security.oauth2.apple.AppleLoginRequest;
import konkuk.thip.common.security.oauth2.apple.AppleTokenClient;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.user.domain.value.Alias;
import konkuk.thip.user.domain.value.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("[통합] POST /auth/apple Apple 로그인 API 테스트")
class AppleLoginApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @MockBean
    private AppleIdentityTokenVerifier appleIdentityTokenVerifier;

    @MockBean
    private AppleTokenClient appleTokenClient;

    private static final String APPLE_SUB = "apple_test.001.abc";
    private static final String OAUTH2_ID = "apple_" + APPLE_SUB;

    @BeforeEach
    void setUp() {
        given(appleIdentityTokenVerifier.verify(anyString())).willReturn(APPLE_SUB);
        given(appleTokenClient.exchangeAuthorizationCode(anyString())).willReturn("apple_refresh_token");
    }

    @Test
    @DisplayName("신규 유저: isNewUser=true, SignupToken 반환")
    void appleLogin_newUser_returnsSignupToken() throws Exception {
        AppleLoginRequest request = new AppleLoginRequest("fake.identity.token", "fake_auth_code");

        mockMvc.perform(post("/auth/apple")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isNewUser").value(true))
                .andExpect(jsonPath("$.data.token").isNotEmpty());
    }

    @Test
    @DisplayName("기존 유저: isNewUser=false, AccessToken 반환")
    void appleLogin_existingUser_returnsAccessToken() throws Exception {
        // given: Apple 유저 DB에 미리 저장
        UserJpaEntity existingUser = UserJpaEntity.builder()
                .nickname("기존애플유저")
                .oauth2Id(OAUTH2_ID)
                .alias(Alias.WRITER)
                .role(UserRole.USER)
                .build();
        userJpaRepository.save(existingUser);

        AppleLoginRequest request = new AppleLoginRequest("fake.identity.token", "fake_auth_code");

        mockMvc.perform(post("/auth/apple")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isNewUser").value(false))
                .andExpect(jsonPath("$.data.token").isNotEmpty());
    }

    @Test
    @DisplayName("identityToken이 없으면 400 반환")
    void appleLogin_missingIdentityToken_returns400() throws Exception {
        AppleLoginRequest request = new AppleLoginRequest("", "fake_auth_code");

        mockMvc.perform(post("/auth/apple")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("identityToken 검증 실패 시 401 반환")
    void appleLogin_invalidIdentityToken_returns401() throws Exception {
        given(appleIdentityTokenVerifier.verify(anyString()))
                .willThrow(new konkuk.thip.common.exception.AuthException(
                        konkuk.thip.common.exception.code.ErrorCode.AUTH_APPLE_IDENTITY_TOKEN_INVALID));

        AppleLoginRequest request = new AppleLoginRequest("invalid.token.here", "fake_auth_code");

        mockMvc.perform(post("/auth/apple")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
