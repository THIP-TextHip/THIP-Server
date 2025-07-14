package konkuk.thip.user.application.port.out;

import java.util.List;
import java.util.Map;

public interface FollowingQueryPort {
    Map<Long, Integer> countByFollowingUserIds(List<Long> userIds);
}

