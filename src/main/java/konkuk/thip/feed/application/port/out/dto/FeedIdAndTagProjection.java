package konkuk.thip.feed.application.port.out.dto;

import konkuk.thip.feed.adapter.out.jpa.TagJpaEntity;

public interface FeedIdAndTagProjection {
    Long getFeedId();
    TagJpaEntity getTagJpaEntity();
}