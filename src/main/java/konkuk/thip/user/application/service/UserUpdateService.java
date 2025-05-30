package konkuk.thip.user.application.service;

import konkuk.thip.user.application.port.in.UserUpdateUseCase;
import konkuk.thip.user.application.port.in.dto.UserUpdateCommand;
import konkuk.thip.user.application.port.out.UserCommandPort;
import konkuk.thip.user.application.port.out.UserQueryPort;
import konkuk.thip.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserUpdateService implements UserUpdateUseCase {

    private final UserCommandPort userCommandPort;
    private final UserQueryPort userQueryPort;

    /**
     * 현재 예시코드는 update 시 해당 Domain 엔티티를 DB에서 찾아온 후, 이 도메인 엔티티를 다시 DB에 반영하는 플로우
     * -> 위 과정을 모두 트랜잭션 범위 안에서 수행함으로써 JPA 변경감지 기능 활용 가능
     *
     * 이 방법 말고 update 시에 Domain 엔티티를 새로 생성한 후, 이걸 DB에 update 쿼리를 날리는 방식도 가능함
     * -> 이건 JPA 변경감지 기능을 활용할 수는 없음. 직접 DB에 update 쿼리 날려야함
     */
    @Transactional
    @Override
    public void update(UserUpdateCommand command) {
        // user 엔티티 find
        User user = userQueryPort.findById(command.getId());

        // 비즈니스 로직 수행 ,,,

        // DB i/o
        userCommandPort.update(user);
    }

}
