package konkuk.thip.user.concurrency;

import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.user.domain.value.Alias;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("[동시성] 사용자 팔로우 상태 변경 동시 요청 테스트")
@Tag("concurrency")
public class UserFollowConcurrencyTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserJpaRepository userJpaRepository;
    @Autowired private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("[동시성] 사용자 팔로우 상태 변경 동시 요청 테스트")
    void user_follow_test_in_multi_thread() throws Exception {
        //given
        final int followerCount = 500;

        // 팔로우 당할 유저 생성
        UserJpaEntity targetUser = userJpaRepository.save(TestEntityFactory.createUser(Alias.ARTIST, "target_user"));

        // 팔로우 하는 유저들 생성
        List<Long> followerIds = createUsersRange(followerCount);

        //when
        ExecutorService pool = Executors.newFixedThreadPool(Math.min(followerCount, 100));
        CountDownLatch ready = new CountDownLatch(followerCount);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch finish = new CountDownLatch(followerCount);

        final String requestBody = """
                {
                    "type": true
                }
                """;

        List<Future<Integer>> results = new ArrayList<>(followerCount);

        for (Long followerId : followerIds) {
            results.add(pool.submit(() -> {
                ready.countDown();
                start.await();
                try {
                    mockMvc.perform(
                                    post("/users/following/{followingUserId}", targetUser.getUserId())
                                            .contentType("application/json")
                                            // 컨트롤러의 @UserId Long userId 에 주입
                                            .requestAttr("userId", followerId)
                                            .content(requestBody)
                            )
                            .andExpect(status().isOk());
                    return 200;
                } catch (AssertionError e) {
                    return 400;
                } finally {
                    finish.countDown();
                }
            }));
        }

        ready.await(10, TimeUnit.SECONDS);
        start.countDown();
        finish.await(60, TimeUnit.SECONDS);
        pool.shutdown();

        //then
        long okCount = results.stream()
                .filter(result -> {
                    try {
                        return result.get() == 200;
                    } catch (Exception e) {
                        return false;
                    }
                }).count();

        Long followingRows = jdbcTemplate.query(
                "SELECT COUNT(*) FROM followings WHERE following_user_id = ?",
                ps -> ps.setLong(1, targetUser.getUserId()),
                rs -> {rs.next(); return rs.getLong(1); }
        );

        Long storedFollowerCount = jdbcTemplate.query(
                "SELECT follower_count FROM users WHERE user_id = ?",
                ps -> ps.setLong(1, targetUser.getUserId()),
                rs -> {rs.next(); return rs.getLong(1); }
        );

        System.out.println("=== RESULT ===");
        System.out.println("OK responses : " + okCount);
        System.out.println("followings rows : " + followingRows);
        System.out.println("target user's follower_count : " + storedFollowerCount);

        // 실제 생성된 팔로잉 행 수는 팔로우 요청 횟수보다 적을 수 있다. (데이터 정합성이 깨지는 경우)
        Assertions.assertThat(followingRows).isLessThanOrEqualTo(followerCount);
        // User 테이블의 팔로워 수 컬럼 역시 팔로우 요청 횟수보다 적을 수 있다. (데이터 정합성이 깨지는 경우)
        Assertions.assertThat(storedFollowerCount).isLessThanOrEqualTo(followerCount);

    }

    private List<Long> createUsersRange(int followerCount) {
        List<Long> userIds = new ArrayList<>();
        for (int i = 0; i < followerCount; i++) {
            UserJpaEntity savedUser = userJpaRepository.save(TestEntityFactory.createUser(Alias.WRITER, "follower_user_" + i));
            userIds.add(savedUser.getUserId());
        }
        return userIds;
    }
}
