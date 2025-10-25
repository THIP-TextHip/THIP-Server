package konkuk.thip.feed.adapter.in.web;

import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.feed.adapter.out.jpa.FeedJpaEntity;
import konkuk.thip.feed.adapter.out.persistence.repository.FeedJpaRepository;
import konkuk.thip.post.adapter.out.persistence.repository.PostLikeJpaRepository;
import konkuk.thip.post.application.port.in.dto.PostIsLikeCommand;
import konkuk.thip.post.application.service.PostLikeService;
import konkuk.thip.post.domain.PostType;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.user.domain.value.Alias;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
@Slf4j
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("[단위] 피드 좋아요 상태변경 다중 스레드 테스트")
class FeedChangeLikeStatusConcurrencyTest {

    @Autowired private PostLikeService postLikeService;

    @Autowired private UserJpaRepository userJpaRepository;
    @Autowired private BookJpaRepository bookJpaRepository;
    @Autowired private FeedJpaRepository feedJpaRepository;
    @Autowired private PostLikeJpaRepository postLikeJpaRepository;

    private UserJpaEntity user;
    private BookJpaEntity book;
    private FeedJpaEntity feed;

    @BeforeEach
    void setUp() {
        Alias alias = TestEntityFactory.createLiteratureAlias();
        user = userJpaRepository.save(TestEntityFactory.createUser(alias));
        book = bookJpaRepository.save(TestEntityFactory.createBookWithISBN("9788954682152"));
        feed = feedJpaRepository.save(TestEntityFactory.createFeed(user,book, true));
    }


    @Test
    public void concurrentLikeToggleTest() throws InterruptedException {

        int threadCount = 2;
        int repeat = 10; // 스레드별 몇 번 반복할지
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount * repeat);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        // 각 스레드별로 현재 상태(true/false)를 관리하기 위한 배열
        boolean[] likeStatus = new boolean[threadCount];

        for (int i = 0; i < threadCount; i++) {
            final int userIndex = i;
            executor.submit(() -> {
                likeStatus[userIndex] = true;
                for (int r = 0; r < repeat; r++) {
                    boolean isLike = likeStatus[userIndex];
                    try {
                        postLikeService.changeLikeStatusPost(
                                new PostIsLikeCommand(user.getUserId(), feed.getPostId(), PostType.FEED, isLike)
                        );
                        successCount.getAndIncrement();
                        // 성공했을 때만 현재 상태를 반전
                        likeStatus[userIndex] = !likeStatus[userIndex];
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        failCount.getAndIncrement();
                    } finally {
                        latch.countDown();
                    }
                }
            });
        }

        latch.await();
        executor.shutdown();

        // then
        assertAll(
                () -> assertThat(successCount.get()).isEqualTo(threadCount * repeat),
                () -> assertThat(failCount.get()).isEqualTo(0)
        );
    }


}
