package konkuk.thip.notification.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.notification.adapter.out.jpa.NotificationJpaEntity;
import konkuk.thip.notification.adapter.out.persistence.repository.NotificationJpaRepository;
import konkuk.thip.notification.domain.value.MessageRoute;
import konkuk.thip.notification.domain.value.NotificationCategory;
import konkuk.thip.notification.domain.value.NotificationRedirectSpec;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.user.domain.value.Alias;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static konkuk.thip.common.exception.code.ErrorCode.NOTIFICATION_ALREADY_CHECKED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("[통합] 알림 읽음 처리 api 통합 테스트")
class NotificationMarkToCheckedApiTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Autowired private UserJpaRepository userJpaRepository;
    @Autowired private NotificationJpaRepository notificationJpaRepository;
    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private EntityManager em;

    @Test
    @DisplayName("본인의 알림을 읽음 처리할 경우, 해당 알림의 isChecked가 true로 변경되고, 알림의 리다이렉트를 위한 데이터가 반환된다.")
    void mark_notification_to_checked_success() throws Exception {
        //given
        UserJpaEntity user = userJpaRepository.save(TestEntityFactory.createUser(Alias.WRITER));

        NotificationRedirectSpec redirectSpec = TestEntityFactory.createNotificationRedirectSpec(
                MessageRoute.FEED_USER, Map.of("userId", 123L)    // 특정 유저의 피드 페이지로 이동
        );

        NotificationJpaEntity notificationJpaEntity = notificationJpaRepository.save(
                TestEntityFactory.createNotification(user, "피드알림", NotificationCategory.FEED, redirectSpec));

        // when & then
        String body = objectMapper.writeValueAsString(Map.of("notificationId", notificationJpaEntity.getNotificationId()));
        mockMvc.perform(post("/notifications/check")
                        .contentType(APPLICATION_JSON)
                        .content(body)
                        .requestAttr("userId", user.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.route").value(MessageRoute.FEED_USER.name()))
                .andExpect(jsonPath("$.data.params.userId").value(123));

        // DB 반영 확인
        NotificationJpaEntity reloaded = notificationJpaRepository.findById(notificationJpaEntity.getNotificationId()).orElseThrow();
        assertThat(reloaded.isChecked()).isTrue();
    }

    @Test
    @DisplayName("다른 사용자의 알림을 읽음 처리하려고 하면, 403(FORBIDDEN) 에러를 반환한다")
    void mark_notification_to_checked_forbidden_when_not_owner() throws Exception {
        // given: 알림 주인(owner)과 다른 사용자(stranger)
        UserJpaEntity owner = userJpaRepository.save(TestEntityFactory.createUser(Alias.WRITER));
        UserJpaEntity stranger = userJpaRepository.save(TestEntityFactory.createUser(Alias.WRITER));

        NotificationJpaEntity notification = notificationJpaRepository.save(
                TestEntityFactory.createNotification(owner, "남의 알림", NotificationCategory.FEED));

        // when & then: 남의 알림을 stranger가 읽음 처리 시도 → 403
        String body = objectMapper.writeValueAsString(Map.of("notificationId", notification.getNotificationId()));
        mockMvc.perform(post("/notifications/check")
                        .contentType(APPLICATION_JSON)
                        .content(body)
                        .requestAttr("userId", stranger.getUserId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("이미 읽음 처리된 알림을 다시 읽음 처리하면, 400 에러를 반환한다.")
    void mark_notification_to_checked_already_checked() throws Exception {
        // given: owner의 알림을 미리 is_checked=true 상태로 만들어 둔다
        UserJpaEntity owner = userJpaRepository.save(TestEntityFactory.createUser(Alias.WRITER));

        NotificationJpaEntity notification = notificationJpaRepository.save(
                TestEntityFactory.createNotification(owner, "이미 읽은 알림", NotificationCategory.FEED));

        // is_checked=true 로 강제 세팅
        jdbcTemplate.update(
                "UPDATE notifications SET is_checked = TRUE WHERE notification_id = ?",
                notification.getNotificationId()
        );
        em.flush();
        em.clear(); // 영속성 컨텍스트 초기화(DB에 직접 반영한 엔티티 변경사항을 반영하기 위해)

        // when & then: 다시 읽음 처리 시도 → 400
        String body = objectMapper.writeValueAsString(Map.of("notificationId", notification.getNotificationId()));
        mockMvc.perform(post("/notifications/check")
                        .contentType(APPLICATION_JSON)
                        .content(body)
                        .requestAttr("userId", owner.getUserId()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(NOTIFICATION_ALREADY_CHECKED.getCode()))
                .andExpect(jsonPath("$.message", containsString(NOTIFICATION_ALREADY_CHECKED.getMessage())));
    }
}
