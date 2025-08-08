package konkuk.thip.feed.application.service;

import jakarta.transaction.Transactional;
import konkuk.thip.feed.application.port.in.FeedDeleteUseCase;
import konkuk.thip.feed.application.port.out.FeedCommandPort;
import konkuk.thip.feed.domain.Feed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FeedDeleteService implements FeedDeleteUseCase {

    private final FeedCommandPort feedCommandPort;

    @Override
    @Transactional
    public void deleteFeed(Long feedId, Long userId) {

        // 1. 피드 조회 및 검증
        Feed feed = feedCommandPort.getByIdOrThrow(feedId);

        // 2. 피드 삭제 권한 검증
        feed.validateDeletable(userId);

        // TODO S3 이미지 삭제 이벤트 기반 처리 or 배치 삭제
        // 3. 피드 삭제
        feedCommandPort.delete(feed);
    }
}
