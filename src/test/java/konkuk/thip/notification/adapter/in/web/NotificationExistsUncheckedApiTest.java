package konkuk.thip.notification.adapter.in.web;

import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.notification.adapter.out.jpa.NotificationJpaEntity;
import konkuk.thip.notification.adapter.out.persistence.repository.NotificationJpaRepository;
import konkuk.thip.notification.domain.value.NotificationCategory;
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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("[통합] 안읽은 알림 존재 여부 확인 api 통합 테스트")
class NotificationExistsUncheckedApiTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserJpaRepository userJpaRepository;
    @Autowired private NotificationJpaRepository notificationJpaRepository;
    @Autowired private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("유저가 읽지 않은 알림이 있을 경우, true 를 반환한다.")
    void notification_exists_unchecked_true() throws Exception {
        //given
        UserJpaEntity user = userJpaRepository.save(TestEntityFactory.createUser(Alias.WRITER));
        NotificationJpaEntity n1 = notificationJpaRepository.save(TestEntityFactory.createNotification(user, "알림1", NotificationCategory.FEED));

        //when
        ResultActions result = mockMvc.perform(get("/notifications/exists-unchecked")
                .requestAttr("userId", user.getUserId()));

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.exists").value(true));
    }

    @Test
    @DisplayName("유저가 읽지 않은 알림이 없을 경우, false 를 반환한다.")
    void notification_exists_unchecked_false() throws Exception {
        //given
        UserJpaEntity user = userJpaRepository.save(TestEntityFactory.createUser(Alias.WRITER));
        NotificationJpaEntity n1 = notificationJpaRepository.save(TestEntityFactory.createNotification(user, "알림1", NotificationCategory.FEED));
        jdbcTemplate.update(
                "UPDATE notifications SET is_checked = TRUE WHERE notification_id = ?",
                n1.getNotificationId()
        );

        //when
        ResultActions result = mockMvc.perform(get("/notifications/exists-unchecked")
                .requestAttr("userId", user.getUserId()));

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.exists").value(false));
    }
}
