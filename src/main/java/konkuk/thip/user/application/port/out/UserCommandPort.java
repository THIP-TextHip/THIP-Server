package konkuk.thip.user.application.port.out;

import konkuk.thip.user.domain.User;

public interface UserCommandPort {

    Long save(User user);

    void update(User user);
}
