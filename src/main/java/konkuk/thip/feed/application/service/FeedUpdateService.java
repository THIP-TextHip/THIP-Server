package konkuk.thip.feed.application.service;

import konkuk.thip.feed.application.port.in.FeedUpdateUseCase;
import konkuk.thip.feed.application.port.in.dto.FeedUpdateCommand;
import konkuk.thip.feed.application.port.out.FeedCommandPort;
import konkuk.thip.feed.domain.Feed;
import konkuk.thip.feed.domain.value.Tag;
import konkuk.thip.feed.domain.value.TagList;
import konkuk.thip.feed.domain.value.ContentList;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FeedUpdateService implements FeedUpdateUseCase {

    private final FeedCommandPort feedCommandPort;

    @Override
    @Transactional
    //TODO 추후 이벤트 기반으로 트랜잭션 커밋후 S3 삭제하도록 리펙토링 or 사용하지 않는 이미지 배치 삭제방식 논의
    public Long updateFeed(FeedUpdateCommand command) {

        //1. 유효성 검증
        TagList.validateTags(Tag.fromList(command.tagList()));
        ContentList.validateImageCount(command.remainImageUrls() != null ? command.remainImageUrls().size() : 0);

        // 2. 피드 조회
        Feed feed = feedCommandPort.getByIdOrThrow(command.feedId());

        // 3. 도메인 내에서 내부 상태 변경 및 검증
        applyPartialFeedUpdate(feed, command);

        // 4. 업데이트
        return feedCommandPort.update(feed);
    }

    private void applyPartialFeedUpdate(Feed feed, FeedUpdateCommand command) {

        if (command.remainImageUrls() != null) {
            feed.updateImages(command.userId(), command.remainImageUrls());
        }
        if (command.contentBody() != null) {
            feed.updateContent(command.userId(), command.contentBody());
        }
        if (command.isPublic() != null) {
            feed.updateVisibility(command.userId(), command.isPublic());
        }
        if (command.tagList() != null) {
            feed.updateTags(command.userId(), command.tagList());
        }
    }
}
