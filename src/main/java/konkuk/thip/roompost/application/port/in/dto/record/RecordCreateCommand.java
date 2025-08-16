package konkuk.thip.roompost.application.port.in.dto.record;

public record RecordCreateCommand(
        Long userId,

        Long roomId,

        int page,

        boolean isOverview,

        String content
) {

}
