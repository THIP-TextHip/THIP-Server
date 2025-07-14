package konkuk.thip.user.application.port.in;


import konkuk.thip.user.application.port.in.dto.UserFollowCommand;

public interface UserFollowUsecase {
    Boolean changeFollowingState(UserFollowCommand followCommand);
}
