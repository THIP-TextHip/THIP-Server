package konkuk.thip.user.application.port.out;

import konkuk.thip.user.domain.User;

public interface UserQueryPort {

    User findById(Long id);
}
