package konkuk.thip.room.adapter.in.web.request;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.*;
import konkuk.thip.room.application.port.in.dto.RoomCreateCommand;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public record RoomCreateRequest(
        @NotBlank(message = "ISBN은 필수입니다.")
        String isbn,

        @NotBlank(message = "카테고리는 필수입니다.")
        String category,

        @NotBlank(message = "방 이름은 필수입니다.")
        String roomName,

        @NotBlank(message = "설명은 필수입니다.")
        String description,

        @Pattern(
                regexp = "\\d{4}\\.\\d{2}\\.\\d{2}",
                message = "진행 시작일은 yyyy.MM.dd 형식이어야 합니다."
        )
        String progressStartDate,

        @Pattern(
                regexp = "\\d{4}\\.\\d{2}\\.\\d{2}",
                message = "진행 종료일은 yyyy.MM.dd 형식이어야 합니다."
        )
        String progressEndDate,

        @Min(value = 1, message = "모집 인원은 최소 1명이어야 합니다.")
        int recruitCount,

        @Nullable
        @Pattern(regexp = "\\d{4}", message = "비밀번호는 숫자 4자리여야 합니다.")
        String password,

        @NotNull(message = "방 공개 설정 여부는 필수입니다.")
        Boolean isPublic
) {
        public RoomCreateCommand toCommand() {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
                return new RoomCreateCommand(
                        isbn,
                        category,
                        roomName,
                        description,
                        LocalDate.parse(progressStartDate, formatter),
                        LocalDate.parse(progressEndDate, formatter),
                        recruitCount,
                        password,
                        isPublic
                );
        }
}
