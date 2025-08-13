package konkuk.thip.feed.adapter.in.web.response;

import konkuk.thip.feed.application.port.in.dto.TagsWithCategoryResult;

import java.util.List;

public record FeedShowWriteInfoResponse(
        List<TagsWithCategoryResult> categoryList
) {
    public static FeedShowWriteInfoResponse of(List<TagsWithCategoryResult> categoryList) {
        return new FeedShowWriteInfoResponse(categoryList);
    }
}
