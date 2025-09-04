package konkuk.thip.user.application.port.out;

import konkuk.thip.user.domain.User;

import java.util.List;
import java.util.Map;

public interface UserCommandPort {

    Long save(User user);
    User findById(Long userId);
    Map<Long, User> findByIds(List<Long> userIds);
    void update(User user);
    void delete(User user);
}
