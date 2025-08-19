package konkuk.thip.feed.application.port.out.dto;

public interface FeedIdAndTagProjection {
    Long getFeedId();
    TagJpaEntity getTagJpaEntity();
}