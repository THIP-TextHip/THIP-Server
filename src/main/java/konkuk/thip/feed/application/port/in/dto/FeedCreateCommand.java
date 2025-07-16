package konkuk.thip.feed.application.port.in.dto;

import java.util.List;

public record FeedCreateCommand(

        String isbn,

        String contentBody,

        List<String> imageUrls,

        Boolean isPublic,

        String category,

        List<String> tagList,

        Long userId
)
{
}
