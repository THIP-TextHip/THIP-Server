package konkuk.thip.feed.domain;

import konkuk.thip.common.exception.InvalidStateException;
import lombok.Getter;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static konkuk.thip.common.exception.code.ErrorCode.*;

@Getter
public class SavedFeeds {

    private final Set<Feed> feeds;

    public SavedFeeds(List<Feed> feeds) {
        Set<Feed> feedSet = new HashSet<>(feeds);
        if (feedSet.size() != feeds.size()) {
            throw new InvalidStateException(DUPLICATED_FEEDS_IN_COLLECTION);
        }
        this.feeds = Collections.unmodifiableSet(feedSet);
    }

    // 중복 저장 검증
    public void validateNotAlreadySaved(Feed feed) {
        if (feeds.contains(feed)) {
            throw new InvalidStateException(FEED_ALREADY_SAVED);
        }
    }

    // 삭제 가능 여부 검증
    public void validateCanDelete(Feed feed) {
        if (!feeds.contains(feed)) {
            throw new InvalidStateException(FEED_NOT_SAVED_CANNOT_DELETE);
        }
    }

}


