package konkuk.thip.user.adapter.out.persistence.repository;

import java.util.Set;

public interface UserQueryRepository {
    Set<Long> findUserIdsByBookId(Long bookId);
}