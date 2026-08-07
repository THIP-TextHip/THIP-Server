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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("[통합] 알림 모두 읽음 처리 api 통합 테스트")
class NotificationMarkAllToCheckedApiTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private UserJpaRepository userJpaRepository;
    @Autowired private NotificationJpaRepository notificationJpaRepository;

    @Test
    @DisplayName("본인의 읽지 않은 모든 알림이 읽음 처리되고, 다른 유저의 알림은 영향받지 않는다.")
    void mark_all_notifications_to_checked_success() throws Exception {
        // given
        UserJpaEntity owner = userJpaRepository.save(TestEntityFactory.createUser(Alias.WRITER));
        UserJpaEntity other = userJpaRepository.save(TestEntityFactory.createUser(Alias.WRITER));

        NotificationJpaEntity unchecked1 = notificationJpaRepository.save(
                TestEntityFactory.createNotification(owner, "알림1", NotificationCategory.FEED));
        NotificationJpaEntity unchecked2 = notificationJpaRepository.save(
                TestEntityFactory.createNotification(owner, "알림2", NotificationCategory.FEED));
        NotificationJpaEntity otherUserNotification = notificationJpaRepository.save(
                TestEntityFactory.createNotification(other, "다른 유저 알림", NotificationCategory.FEED));

        // when & then
        mockMvc.perform(post("/notifications/check-all")
                        .requestAttr("userId", owner.getUserId()))
                .andExpect(status().isOk());

        assertThat(notificationJpaRepository.findById(unchecked1.getNotificationId()).orElseThrow().isChecked()).isTrue();
        assertThat(notificationJpaRepository.findById(unchecked2.getNotificationId()).orElseThrow().isChecked()).isTrue();
        assertThat(notificationJpaRepository.findById(otherUserNotification.getNotificationId()).orElseThrow().isChecked()).isFalse();
    }

    @Test
    @DisplayName("읽지 않은 알림이 없는 경우에도 에러 없이 성공한다.")
    void mark_all_notifications_to_checked_when_none_unchecked() throws Exception {
        // given
        UserJpaEntity owner = userJpaRepository.save(TestEntityFactory.createUser(Alias.WRITER));

        // when & then
        mockMvc.perform(post("/notifications/check-all")
                        .requestAttr("userId", owner.getUserId()))
                .andExpect(status().isOk());
    }
}
