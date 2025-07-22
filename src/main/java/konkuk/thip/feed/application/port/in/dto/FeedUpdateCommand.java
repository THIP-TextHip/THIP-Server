package konkuk.thip.feed.application.port.in.dto;

import java.util.List;

public record FeedUpdateCommand(

        String contentBody,

        Boolean isPublic,

        List<String> tagList,

        List<String> remainImageUrls,

        Long userId,

        Long feedId
)
{
}
