package konkuk.thip.user.application.port.in;

import konkuk.thip.user.application.port.in.dto.UserReadResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserQueryFacade {

    /**
     * 여기에 query 관련 use case(= incoming port = interface) 전부 주입
     */
    private final UserReadUseCase userReadUseCase;

    public UserReadResult readUser(Long id) {
        return userReadUseCase.readUser(id);
    }
}
