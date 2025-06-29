package konkuk.thip.feed.application.port.out;

import java.util.Set;

public interface FeedQueryPort {

    Set<Long> findUserIdsByBookId(Long bookId);
}
