package konkuk.thip.room.application.port.in.dto;

import java.time.LocalDate;

public record RoomCreateCommand(
        String isbn,

        String category,

        String roomName,

        String description,

        LocalDate progressStartDate,

        LocalDate progressEndDate,

        int recruitCount,

        String password,

        Boolean isPublic
) {
}
