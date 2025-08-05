package konkuk.thip.user.adapter.out.persistence.function;

import konkuk.thip.user.application.port.out.dto.ReactionQueryDto;

import java.time.LocalDateTime;
import java.util.List;

@FunctionalInterface
public interface ReactionQueryFunction {
    List<ReactionQueryDto> fetch(Long userId, LocalDateTime cursorDateTime, int size);
}
