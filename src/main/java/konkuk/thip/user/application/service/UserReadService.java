package konkuk.thip.user.application.service;

import konkuk.thip.user.application.port.in.UserReadUseCase;
import konkuk.thip.user.application.port.in.dto.UserReadResult;
import konkuk.thip.user.application.port.out.UserQueryPort;
import konkuk.thip.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserReadService implements UserReadUseCase {

    private final UserQueryPort userQueryPort;

    /**
     * 얜 트랜잭션 없어도 ok
     */
    @Override
    public UserReadResult readUser(Long id) {
        User user = userQueryPort.findById(id);

        return UserReadResult.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }
}
