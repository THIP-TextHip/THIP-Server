package konkuk.thip.user.application.service.block;

import konkuk.thip.common.util.CursorBasedList;
import konkuk.thip.user.adapter.in.web.response.UserBlockedListResponse;
import konkuk.thip.user.application.mapper.BlockQueryMapper;
import konkuk.thip.user.application.port.in.UserGetBlockedUsersUseCase;
import konkuk.thip.user.application.port.out.UserBlockQueryPort;
import konkuk.thip.user.application.port.out.UserCommandPort;
import konkuk.thip.user.application.port.out.dto.BlockedUserQueryDto;
import konkuk.thip.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserGetBlockedUsersService implements UserGetBlockedUsersUseCase {

    private final UserBlockQueryPort userBlockQueryPort;
    private final UserCommandPort userCommandPort;

    private final BlockQueryMapper blockQueryMapper;

    private static final int MAX_PAGE_SIZE = 10;

    @Override
    @Transactional(readOnly = true)
    public UserBlockedListResponse getBlockedUsers(Long userId, String cursor, int size) {
        User user = userCommandPort.findById(userId);

        Integer totalBlockedUserCount = (cursor == null || cursor.isBlank()) ?
                userBlockQueryPort.getBlockedUserCountByUserId(user.getId()) : null;

        CursorBasedList<BlockedUserQueryDto> result = userBlockQueryPort.getBlockedUsersByUserId(
                user.getId(), cursor, Math.min(size, MAX_PAGE_SIZE)
        );

        return UserBlockedListResponse.builder()
                .blockedUsers(blockQueryMapper.toBlockedUserDtoList(result.contents()))
                .totalBlockedUserCount(totalBlockedUserCount)
                .nextCursor(result.nextCursor())
                .isLast(result.isLast())
                .build();
    }
}
