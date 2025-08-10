package konkuk.thip.user.application.port.out;

import konkuk.thip.common.exception.EntityNotFoundException;
import konkuk.thip.user.domain.User;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static konkuk.thip.common.exception.code.ErrorCode.USER_NOT_FOUND;

public interface UserCommandPort {

    Long save(User user);
    Optional<User> findById(Long id);
    default User getByIdOrThrow(Long id) {
        return findById(id)
                .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND));
    }
    Map<Long, User> findByIds(List<Long> userIds);
    void update(User user);
    void delete(User user);
}
