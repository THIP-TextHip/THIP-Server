package konkuk.thip.feed.application.service;

import konkuk.thip.feed.adapter.in.web.response.FeedShowAllResponse;
import konkuk.thip.feed.application.port.in.FeedShowAllUseCase;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(
        name = "feed.show.strategy",
        havingValue = "personal"   // 프로퍼티가 personal 이면 이 구현체 사용
)
public class PersonalizedFeedShowAllService implements FeedShowAllUseCase {

    /**
     * 추후 구현될 "유저 맞춤 피드 기능이 추가된" 피드 목록 조회를 위한 구현체
     */

    @Override
    public FeedShowAllResponse showAllFeeds(Long userId, String cursor) {
        return null;
    }
}
