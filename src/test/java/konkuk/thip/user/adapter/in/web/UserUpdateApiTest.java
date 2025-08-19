package konkuk.thip.user.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.user.adapter.in.web.request.UserUpdateRequest;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserRole;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.user.domain.Alias;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("[통합] 사용자 정보 수정 API 통합 테스트")
class UserUpdateApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @AfterEach
    void tearDown() {
        userJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("사용자 닉네임과 별칭이 정상적으로 업데이트된다.")
    void updateUser_success() throws Exception {
        // given: 기존 alias 및 user 생성
        Alias oldAlias = TestEntityFactory.createScienceAlias();
        Alias newAlias = TestEntityFactory.createLiteratureAlias();

        UserJpaEntity user = userJpaRepository.save(UserJpaEntity.builder()
                .nickname("oldthip")
                .nicknameUpdatedAt(LocalDate.now().minusMonths(7))
                .oauth2Id("oauth2_user100")
                .role(UserRole.USER)
                .alias(oldAlias)
                .build());

        Long userId = user.getUserId();

        UserUpdateRequest userUpdateRequest = new UserUpdateRequest(newAlias.getValue(),"newthip");

        // when: API 요청
        mockMvc.perform(patch("/users")
                        .requestAttr("userId", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdateRequest)))
                // then: HTTP 응답 상태 및 기본 응답 검증
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").doesNotExist());

        // DB 검증: 닉네임과 alias가 업데이트되었는지 확인
        UserJpaEntity updatedUser = userJpaRepository.findById(userId).orElseThrow();
        assertThat(updatedUser.getNickname()).isEqualTo("newthip");
        assertThat(updatedUser.getAlias()).isEqualTo(newAlias);
    }
}
