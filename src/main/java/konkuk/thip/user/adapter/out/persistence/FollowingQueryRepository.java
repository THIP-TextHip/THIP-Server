package konkuk.thip.user.adapter.out.persistence;

import java.util.List;
import java.util.Map;

public interface FollowingQueryRepository {
    Map<Long, Integer> countByFollowingUserIds(List<Long> userIds);
}
