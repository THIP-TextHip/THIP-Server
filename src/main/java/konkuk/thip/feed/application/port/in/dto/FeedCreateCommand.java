package konkuk.thip.feed.application.port.in.dto;

import java.util.List;

public record FeedCreateCommand(

        String isbn,

        String contentBody,

        Boolean isPublic,

        List<String> tagList,

        Long userId
)
{
}
