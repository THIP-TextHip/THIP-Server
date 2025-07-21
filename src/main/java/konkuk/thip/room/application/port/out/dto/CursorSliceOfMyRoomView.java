package konkuk.thip.room.application.port.out.dto;

import lombok.Getter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.SliceImpl;

import java.time.LocalDate;
import java.util.List;

@Getter
public class CursorSliceOfMyRoomView<T> extends SliceImpl<T> {

    private final LocalDate nextCursorDate;
    private final Long nextCursorId;

    public CursorSliceOfMyRoomView(List<T> content, Pageable pageable, boolean hasNext,
                                   LocalDate nextCursorDate, Long nextCursorId) {
        super(content, pageable, hasNext);
        this.nextCursorDate = nextCursorDate;
        this.nextCursorId = nextCursorId;
    }
}
