package konkuk.thip.notification.adapter.in.web;

import com.jayway.jsonpath.JsonPath;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.*;


@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("[통합] 알림센터 조회 api 통합 테스트")
@Transactional
class NotificationShowApiTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserJpaRepository userJpaRepository;
    @Autowired private NotificationJpaRepository notificationJpaRepository;
    @Autowired private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("type == feed : 유저의 피드 알림을 최신순으로 반환한다.")
    void show_feed_notifications() throws Exception {
        //given
        UserJpaEntity user = userJpaRepository.save(TestEntityFactory.createUser(Alias.WRITER));

        NotificationJpaEntity feedN_1 = notificationJpaRepository.save(TestEntityFactory.createNotification(user, "피드알림_1", NotificationCategory.FEED));
        NotificationJpaEntity feedN_2 = notificationJpaRepository.save(TestEntityFactory.createNotification(user, "피드알림_2",  NotificationCategory.FEED));

        LocalDateTime now = LocalDateTime.now();
        jdbcTemplate.update(
                "UPDATE notifications SET created_at = ? WHERE notification_id = ?",
                now.minusMinutes(30), feedN_1.getNotificationId()
        );
        jdbcTemplate.update(
                "UPDATE notifications SET created_at = ? WHERE notification_id = ?",
                now.minusMinutes(10), feedN_2.getNotificationId()
        );

        //when
        ResultActions result = mockMvc.perform(get("/notifications")
                .requestAttr("userId", user.getUserId())
                .param("type", "feed"));    // type == feed

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.notifications", hasSize(2)))
                .andExpect(jsonPath("$.data.isLast").value(true))
                // 정렬 순서 : 최신순 (feedN_2 -> feedN_1)
                .andExpect(jsonPath("$.data.notifications[0].notificationId").value(feedN_2.getNotificationId()))
                .andExpect(jsonPath("$.data.notifications[0].title").value("피드알림_2"))
                .andExpect(jsonPath("$.data.notifications[0].notificationType").value(NotificationCategory.FEED.getDisplay()))
                .andExpect(jsonPath("$.data.notifications[1].notificationId").value(feedN_1.getNotificationId()))
                .andExpect(jsonPath("$.data.notifications[1].title").value("피드알림_1"))
                .andExpect(jsonPath("$.data.notifications[1].notificationType").value(NotificationCategory.FEED.getDisplay()));
    }

    @Test
    @DisplayName("type == room : 유저의 모임 알림을 최신순으로 반환한다.")
    void show_room_notifications() throws Exception {
        //given
        UserJpaEntity user = userJpaRepository.save(TestEntityFactory.createUser(Alias.WRITER));

        NotificationJpaEntity roomN_1 = notificationJpaRepository.save(TestEntityFactory.createNotification(user, "모임알림_1", NotificationCategory.ROOM));
        NotificationJpaEntity roomN_2 = notificationJpaRepository.save(TestEntityFactory.createNotification(user, "모임알림_2",  NotificationCategory.ROOM));

        LocalDateTime now = LocalDateTime.now();
        jdbcTemplate.update(
                "UPDATE notifications SET created_at = ? WHERE notification_id = ?",
                now.minusMinutes(30), roomN_1.getNotificationId()
        );
        jdbcTemplate.update(
                "UPDATE notifications SET created_at = ? WHERE notification_id = ?",
                now.minusMinutes(10), roomN_2.getNotificationId()
        );

        //when
        ResultActions result = mockMvc.perform(get("/notifications")
                .requestAttr("userId", user.getUserId())
                .param("type", "room"));    // type == room

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.notifications", hasSize(2)))
                .andExpect(jsonPath("$.data.isLast").value(true))
                // 정렬 순서 : 최신순 (roomN_2 -> roomN_1)
                .andExpect(jsonPath("$.data.notifications[0].notificationId").value(roomN_2.getNotificationId()))
                .andExpect(jsonPath("$.data.notifications[0].title").value("모임알림_2"))
                .andExpect(jsonPath("$.data.notifications[0].notificationType").value(NotificationCategory.ROOM.getDisplay()))
                .andExpect(jsonPath("$.data.notifications[1].notificationId").value(roomN_1.getNotificationId()))
                .andExpect(jsonPath("$.data.notifications[1].title").value("모임알림_1"))
                .andExpect(jsonPath("$.data.notifications[1].notificationType").value(NotificationCategory.ROOM.getDisplay()));
    }

    @Test
    @DisplayName("type == null : type이 null 일 경우, 유저의 피드 & 모임 알림을 최신순으로 반환한다.")
    void show_feed_and_room_notifications() throws Exception {
        //given
        UserJpaEntity user = userJpaRepository.save(TestEntityFactory.createUser(Alias.WRITER));

        NotificationJpaEntity n_feed_1 = notificationJpaRepository.save(TestEntityFactory.createNotification(user, "알림_피드_1", NotificationCategory.FEED));
        NotificationJpaEntity n_feed_2 = notificationJpaRepository.save(TestEntityFactory.createNotification(user, "알림_피드_2",  NotificationCategory.FEED));
        NotificationJpaEntity n_room_3 = notificationJpaRepository.save(TestEntityFactory.createNotification(user, "알림_모임_3", NotificationCategory.ROOM));
        NotificationJpaEntity n_room_4 = notificationJpaRepository.save(TestEntityFactory.createNotification(user, "알림_모임_4",  NotificationCategory.ROOM));

        LocalDateTime now = LocalDateTime.now();
        jdbcTemplate.update(
                "UPDATE notifications SET created_at = ? WHERE notification_id = ?",
                now.minusMinutes(30), n_feed_1.getNotificationId()
        );
        jdbcTemplate.update(
                "UPDATE notifications SET created_at = ? WHERE notification_id = ?",
                now.minusMinutes(20), n_feed_2.getNotificationId()
        );
        jdbcTemplate.update(
                "UPDATE notifications SET created_at = ? WHERE notification_id = ?",
                now.minusMinutes(10), n_room_3.getNotificationId()
        );
        jdbcTemplate.update(
                "UPDATE notifications SET created_at = ? WHERE notification_id = ?",
                now.minusMinutes(5), n_room_4.getNotificationId()
        );

        //when
        ResultActions result = mockMvc.perform(get("/notifications")
                .requestAttr("userId", user.getUserId()));  // type == null

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.notifications", hasSize(4)))
                .andExpect(jsonPath("$.data.isLast").value(true))
                // 정렬 순서 : 최신순 (n_room_4 -> n_room_3 -> n_feed_2 -> n_feed_1)
                .andExpect(jsonPath("$.data.notifications[0].notificationId").value(n_room_4.getNotificationId()))
                .andExpect(jsonPath("$.data.notifications[0].title").value("알림_모임_4"))
                .andExpect(jsonPath("$.data.notifications[0].notificationType").value(NotificationCategory.ROOM.getDisplay()))
                .andExpect(jsonPath("$.data.notifications[1].notificationId").value(n_room_3.getNotificationId()))
                .andExpect(jsonPath("$.data.notifications[1].title").value("알림_모임_3"))
                .andExpect(jsonPath("$.data.notifications[1].notificationType").value(NotificationCategory.ROOM.getDisplay()))
                .andExpect(jsonPath("$.data.notifications[2].notificationId").value(n_feed_2.getNotificationId()))
                .andExpect(jsonPath("$.data.notifications[2].title").value("알림_피드_2"))
                .andExpect(jsonPath("$.data.notifications[2].notificationType").value(NotificationCategory.FEED.getDisplay()))
                .andExpect(jsonPath("$.data.notifications[3].notificationId").value(n_feed_1.getNotificationId()))
                .andExpect(jsonPath("$.data.notifications[3].title").value("알림_피드_1"))
                .andExpect(jsonPath("$.data.notifications[3].notificationType").value(NotificationCategory.FEED.getDisplay()));
    }

    @Test
    @DisplayName("유저의 알림을 최신순으로 최대 10개 반환한다. 다음 페이지에 해당하는 데이터가 있을 경우, 다음 페이지의 cursor 값을 반환한다.")
    void show_notifications_paging() throws Exception {
        //given
        UserJpaEntity user = userJpaRepository.save(TestEntityFactory.createUser(Alias.WRITER));

        NotificationJpaEntity feedN_1 = notificationJpaRepository.save(TestEntityFactory.createNotification(user, "피드알림_1", NotificationCategory.FEED));
        NotificationJpaEntity feedN_2 = notificationJpaRepository.save(TestEntityFactory.createNotification(user, "피드알림_2",  NotificationCategory.FEED));
        NotificationJpaEntity feedN_3 = notificationJpaRepository.save(TestEntityFactory.createNotification(user, "피드알림_3",  NotificationCategory.FEED));
        NotificationJpaEntity feedN_4 = notificationJpaRepository.save(TestEntityFactory.createNotification(user, "피드알림_4",  NotificationCategory.FEED));
        NotificationJpaEntity feedN_5 = notificationJpaRepository.save(TestEntityFactory.createNotification(user, "피드알림_5",  NotificationCategory.FEED));
        NotificationJpaEntity feedN_6 = notificationJpaRepository.save(TestEntityFactory.createNotification(user, "피드알림_6",  NotificationCategory.FEED));
        NotificationJpaEntity feedN_7 = notificationJpaRepository.save(TestEntityFactory.createNotification(user, "피드알림_7",  NotificationCategory.FEED));
        NotificationJpaEntity feedN_8 = notificationJpaRepository.save(TestEntityFactory.createNotification(user, "피드알림_8",  NotificationCategory.FEED));
        NotificationJpaEntity feedN_9 = notificationJpaRepository.save(TestEntityFactory.createNotification(user, "피드알림_9",  NotificationCategory.FEED));
        NotificationJpaEntity feedN_10 = notificationJpaRepository.save(TestEntityFactory.createNotification(user, "피드알림_10",  NotificationCategory.FEED));
        NotificationJpaEntity feedN_11 = notificationJpaRepository.save(TestEntityFactory.createNotification(user, "피드알림_11",  NotificationCategory.FEED));
        NotificationJpaEntity feedN_12 = notificationJpaRepository.save(TestEntityFactory.createNotification(user, "피드알림_12",  NotificationCategory.FEED));

        LocalDateTime now = LocalDateTime.now();
        jdbcTemplate.update(
                "UPDATE notifications SET created_at = ? WHERE notification_id = ?",
                now.minusMinutes(50), feedN_1.getNotificationId()
        );
        jdbcTemplate.update(
                "UPDATE notifications SET created_at = ? WHERE notification_id = ?",
                now.minusMinutes(45), feedN_2.getNotificationId()
        );
        jdbcTemplate.update(
                "UPDATE notifications SET created_at = ? WHERE notification_id = ?",
                now.minusMinutes(40), feedN_3.getNotificationId()
        );
        jdbcTemplate.update(
                "UPDATE notifications SET created_at = ? WHERE notification_id = ?",
                now.minusMinutes(35), feedN_4.getNotificationId()
        );
        jdbcTemplate.update(
                "UPDATE notifications SET created_at = ? WHERE notification_id = ?",
                now.minusMinutes(30), feedN_5.getNotificationId()
        );
        jdbcTemplate.update(
                "UPDATE notifications SET created_at = ? WHERE notification_id = ?",
                now.minusMinutes(25), feedN_6.getNotificationId()
        );
        jdbcTemplate.update(
                "UPDATE notifications SET created_at = ? WHERE notification_id = ?",
                now.minusMinutes(20), feedN_7.getNotificationId()
        );
        jdbcTemplate.update(
                "UPDATE notifications SET created_at = ? WHERE notification_id = ?",
                now.minusMinutes(15), feedN_8.getNotificationId()
        );
        jdbcTemplate.update(
                "UPDATE notifications SET created_at = ? WHERE notification_id = ?",
                now.minusMinutes(10), feedN_9.getNotificationId()
        );
        jdbcTemplate.update(
                "UPDATE notifications SET created_at = ? WHERE notification_id = ?",
                now.minusMinutes(5), feedN_10.getNotificationId()
        );
        jdbcTemplate.update(
                "UPDATE notifications SET created_at = ? WHERE notification_id = ?",
                now.minusMinutes(3), feedN_11.getNotificationId()
        );
        jdbcTemplate.update(
                "UPDATE notifications SET created_at = ? WHERE notification_id = ?",
                now.minusMinutes(1), feedN_12.getNotificationId()
        );

        //when //then
        MvcResult firstResult = mockMvc.perform(get("/notifications")
                        .requestAttr("userId", user.getUserId())
                        .param("type", "feed"))    // type == feed
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.notifications", hasSize(10)))   // 10개 조회
                .andExpect(jsonPath("$.data.isLast").value(false))          // 다음 페이지 존재
                .andExpect(jsonPath("$.data.nextCursor").isNotEmpty())      // nextCursor 존재
                // 정렬 순서 : 최신순 (feedN_12 -> feedN_3)
                .andExpect(jsonPath("$.data.notifications[0].notificationId").value(feedN_12.getNotificationId()))
                .andExpect(jsonPath("$.data.notifications[0].title").value("피드알림_12"))
                .andExpect(jsonPath("$.data.notifications[1].notificationId").value(feedN_11.getNotificationId()))
                .andExpect(jsonPath("$.data.notifications[1].title").value("피드알림_11"))
                .andExpect(jsonPath("$.data.notifications[2].notificationId").value(feedN_10.getNotificationId()))
                .andExpect(jsonPath("$.data.notifications[2].title").value("피드알림_10"))
                .andExpect(jsonPath("$.data.notifications[3].notificationId").value(feedN_9.getNotificationId()))
                .andExpect(jsonPath("$.data.notifications[3].title").value("피드알림_9"))
                .andExpect(jsonPath("$.data.notifications[4].notificationId").value(feedN_8.getNotificationId()))
                .andExpect(jsonPath("$.data.notifications[4].title").value("피드알림_8"))
                .andExpect(jsonPath("$.data.notifications[5].notificationId").value(feedN_7.getNotificationId()))
                .andExpect(jsonPath("$.data.notifications[5].title").value("피드알림_7"))
                .andExpect(jsonPath("$.data.notifications[6].notificationId").value(feedN_6.getNotificationId()))
                .andExpect(jsonPath("$.data.notifications[6].title").value("피드알림_6"))
                .andExpect(jsonPath("$.data.notifications[7].notificationId").value(feedN_5.getNotificationId()))
                .andExpect(jsonPath("$.data.notifications[7].title").value("피드알림_5"))
                .andExpect(jsonPath("$.data.notifications[8].notificationId").value(feedN_4.getNotificationId()))
                .andExpect(jsonPath("$.data.notifications[8].title").value("피드알림_4"))
                .andExpect(jsonPath("$.data.notifications[9].notificationId").value(feedN_3.getNotificationId()))
                .andExpect(jsonPath("$.data.notifications[9].title").value("피드알림_3"))
                .andReturn();

        String responseBody = firstResult.getResponse().getContentAsString();
        String nextCursor = JsonPath.read(responseBody, "$.data.nextCursor");

        mockMvc.perform(get("/notifications")
                        .requestAttr("userId", user.getUserId())
                        .param("type", "feed")  // type == feed
                        .param("cursor", nextCursor))  // 두번째 페이지 조회
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.notifications", hasSize(2)))   // 2개 조회
                .andExpect(jsonPath("$.data.isLast").value(true))   // 마지막 페이지
                // 정렬 순서 : 최신순 (feedN_2 -> feedN_1)
                .andExpect(jsonPath("$.data.notifications[0].notificationId").value(feedN_2.getNotificationId()))
                .andExpect(jsonPath("$.data.notifications[0].title").value("피드알림_2"))
                .andExpect(jsonPath("$.data.notifications[1].notificationId").value(feedN_1.getNotificationId()))
                .andExpect(jsonPath("$.data.notifications[1].title").value("피드알림_1"));
    }
}
