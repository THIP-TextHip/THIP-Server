package konkuk.thip.feed.application.port.in.dto;

import java.util.List;

public record TagsWithCategoryResult(
    String category,
    List<String> tagList
) {}
