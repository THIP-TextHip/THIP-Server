package konkuk.thip.feed.application.port.in.dto;

public record FeedIsSavedCommand(

        Long userId,

        Long feedId,

        Boolean isSaved
)
{
}
