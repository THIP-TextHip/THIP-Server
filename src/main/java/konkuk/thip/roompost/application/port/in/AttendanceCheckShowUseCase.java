package konkuk.thip.roompost.application.port.in;

import konkuk.thip.roompost.adapter.in.web.response.AttendanceCheckShowResponse;

public interface AttendanceCheckShowUseCase {

    AttendanceCheckShowResponse showDailyGreeting(Long userId, Long roomId, String cursorStr);
}
