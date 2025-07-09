package konkuk.thip.record.application.port.in.dto;

public record RecordCreateCommand(
        Long userId,

        Long roomId,

        int page,

        boolean isOverview,

        String content
) {

}
