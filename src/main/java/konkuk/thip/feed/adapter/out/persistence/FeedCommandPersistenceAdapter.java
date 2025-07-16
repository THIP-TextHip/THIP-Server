package konkuk.thip.feed.adapter.out.persistence;

import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.common.exception.EntityNotFoundException;
import konkuk.thip.feed.adapter.out.jpa.ContentJpaEntity;
import konkuk.thip.feed.adapter.out.jpa.FeedJpaEntity;
import konkuk.thip.feed.adapter.out.jpa.FeedTagJpaEntity;
import konkuk.thip.feed.adapter.out.jpa.TagJpaEntity;
import konkuk.thip.feed.adapter.out.mapper.ContentMapper;
import konkuk.thip.feed.adapter.out.mapper.FeedMapper;
import konkuk.thip.feed.adapter.out.persistence.repository.FeedJpaRepository;
import konkuk.thip.feed.adapter.out.persistence.repository.FeedTag.FeedTagJpaRepository;
import konkuk.thip.feed.adapter.out.persistence.repository.Tag.TagJpaRepository;
import konkuk.thip.feed.application.port.out.FeedCommandPort;
import konkuk.thip.feed.domain.Feed;
import konkuk.thip.feed.domain.Tag;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

import static konkuk.thip.common.exception.code.ErrorCode.*;

@Slf4j
@Repository
@RequiredArgsConstructor
public class FeedCommandPersistenceAdapter implements FeedCommandPort {

    private final FeedJpaRepository feedJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final BookJpaRepository bookJpaRepository;
    private final TagJpaRepository tagJpaRepository;
    private final FeedTagJpaRepository feedTagJpaRepository;
    private final FeedMapper feedMapper;
    private final ContentMapper contentMapper;

    @Override
    public Long save(Feed feed) {

        UserJpaEntity userJpaEntity = userJpaRepository.findById(feed.getCreatorId()).orElseThrow(
                () -> new EntityNotFoundException(USER_NOT_FOUND)
        );
        BookJpaEntity bookJpaEntity = bookJpaRepository.findById(feed.getTargetBookId()).orElseThrow(
                () -> new EntityNotFoundException(BOOK_NOT_FOUND)
        );
        FeedJpaEntity feedJpaEntity = feedMapper.toJpaEntity(feed,userJpaEntity,bookJpaEntity);

        // Feed 먼저 영속화 → ID 생성
        feedJpaRepository.save(feedJpaEntity);

        // Content가 존재하면 ContentJpaEntity 생성 및 Feed 연관관계 설정
        saveContents(feed, feedJpaEntity);
        // 태그가 존재하면 태그 피드 매핑 생성 및 저장
        saveFeedTags(feed, feedJpaEntity);

        return feedJpaRepository.save(feedJpaEntity).getPostId();
    }

    private void saveContents(Feed feed, FeedJpaEntity feedJpaEntity) {
        if (feed.getContentList().isEmpty()) return;

        List<ContentJpaEntity> contentJpaEntities = feed.getContentList().stream()
                .map(content -> contentMapper.toJpaEntity(content, feedJpaEntity))
                .toList();

        contentJpaEntities.forEach(feedJpaEntity.getContentList()::add);
    }

    private void saveFeedTags(Feed feed, FeedJpaEntity feedJpaEntity) {
        if (feed.getTagList().isEmpty()) return;

        for (Tag tag : feed.getTagList()) {
            TagJpaEntity tagJpaEntity = tagJpaRepository.findByValue(tag.getValue())
                    .orElseThrow(() -> new EntityNotFoundException(TAG_NOT_FOUND));

            FeedTagJpaEntity feedTagJpaEntity = FeedTagJpaEntity.builder()
                    .feedJpaEntity(feedJpaEntity)
                    .tagJpaEntity(tagJpaEntity)
                    .build();

            feedTagJpaRepository.save(feedTagJpaEntity);
        }
    }


}
