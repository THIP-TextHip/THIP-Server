package konkuk.thip.feed.application.service;

import konkuk.thip.feed.adapter.in.web.response.FeedShowWriteInfoResponse;
import konkuk.thip.feed.application.mapper.FeedQueryMapper;
import konkuk.thip.feed.application.port.in.FeedShowWriteInfoUseCase;
import konkuk.thip.feed.application.port.out.FeedQueryPort;
import konkuk.thip.feed.application.port.out.dto.TagCategoryQueryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedShowWriteInfoService implements FeedShowWriteInfoUseCase {

    private final FeedQueryPort feedQueryPort;
    private final FeedQueryMapper feedQueryMapper;

    @Override
    public FeedShowWriteInfoResponse showFeedWriteInfo() {
        List<TagCategoryQueryDto> rows = feedQueryPort.findAllTags();
        return FeedShowWriteInfoResponse.of(feedQueryMapper.toTagsWithCategoryResult(rows));
    }

}
