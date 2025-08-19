package konkuk.thip.feed.application.service;

import konkuk.thip.common.util.EnumMappings;
import konkuk.thip.feed.adapter.in.web.response.FeedShowWriteInfoResponse;
import konkuk.thip.feed.application.mapper.FeedQueryMapper;
import konkuk.thip.feed.application.port.in.FeedShowWriteInfoUseCase;
import konkuk.thip.feed.application.port.out.FeedQueryPort;
import konkuk.thip.feed.domain.Tag;
import konkuk.thip.room.domain.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FeedShowWriteInfoService implements FeedShowWriteInfoUseCase {

    private final FeedQueryPort feedQueryPort;
    private final FeedQueryMapper feedQueryMapper;

    @Override
    @Transactional(readOnly = true)
    public FeedShowWriteInfoResponse showFeedWriteInfo() {
        // 1. 태그와 카테고리 조회
        Map<Category, List<Tag>> categoryToTags = EnumMappings.getCategoryToTags();

        return FeedShowWriteInfoResponse.of(feedQueryMapper.toTagsWithCategoryResult(categoryToTags));
    }

}
