package konkuk.thip.user.application.service;

import konkuk.thip.common.exception.BusinessException;
import konkuk.thip.common.exception.code.ErrorCode;
import konkuk.thip.user.application.port.in.UserUpdateUseCase;
import konkuk.thip.user.application.port.in.dto.UserUpdateCommand;
import konkuk.thip.user.application.port.out.UserCommandPort;
import konkuk.thip.user.application.port.out.UserQueryPort;
import konkuk.thip.user.domain.Alias;
import konkuk.thip.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserUpdateService implements UserUpdateUseCase {

    private final UserCommandPort userCommandPort;
    private final UserQueryPort userQueryPort;

    @Override
    @Transactional
    public void updateUser(UserUpdateCommand command) {
        Alias alias = Alias.from(command.aliasName());
        boolean isNicknameUpdateRequest = command.nickname() != null; // 닉네임이 null이 아니면 닉네임 업데이트 요청

        User user = userCommandPort.findById(command.userId());
        user.updateUserInfo(command.nickname(), alias, isNicknameUpdateRequest);

        if(isNicknameUpdateRequest && userQueryPort.existsByNicknameAndUserIdNot(command.nickname(), command.userId())) {
            throw new BusinessException(ErrorCode.USER_NICKNAME_ALREADY_EXISTS);
        }
        userCommandPort.update(user);
    }
}
