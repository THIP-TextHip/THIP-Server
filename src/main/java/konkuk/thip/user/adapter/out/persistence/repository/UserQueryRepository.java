package konkuk.thip.user.adapter.out.persistence.repository;

import konkuk.thip.user.application.port.out.dto.UserQueryDto;

import java.util.List;
import java.util.Set;

public interface UserQueryRepository {
    Set<Long> findUserIdsByBookId(Long bookId);

    List<UserQueryDto> findUsersByNicknameOrderByAccuracy(String keyword, Long userId, Integer size);

}