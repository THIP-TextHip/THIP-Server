package konkuk.thip.outbox.adapter.out.jpa;

import jakarta.persistence.*;
import konkuk.thip.common.entity.BaseJpaEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "outbox_events")
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class OutboxEventJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 도메인에 대한 이벤트인지 (USER, FOLLOW 등)
    private String aggregateType;

    // 연관된 도메인 ID (예: targetUserId)
    private Long aggregateId;

    // 이벤트 타입 (예: USER_FOLLOWED, USER_UNFOLLOWED)
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private OutboxEventType eventType;

    // JSON 문자열로 이벤트 payload 저장
    @Lob
    private String payload;

    // PENDING / PROCESSED / FAILED
    @Enumerated(EnumType.STRING)

    private OutboxStatus outboxStatus;

    private LocalDateTime processedAt;

    // 정적 팩토리 메서드
    public static OutboxEventJpaEntity pending(
            String aggregateType,
            Long aggregateId,
            OutboxEventType eventType,
            String payload
    ) {
        OutboxEventJpaEntity entity = new OutboxEventJpaEntity();
        entity.aggregateType = aggregateType;
        entity.aggregateId = aggregateId;
        entity.eventType = eventType;
        entity.payload = payload;
        entity.outboxStatus = OutboxStatus.PENDING;
        return entity;
    }

    public void markAsProcessed() {
        this.outboxStatus = OutboxStatus.PROCESSED;
        this.processedAt = LocalDateTime.now();
    }

    public void markAsFailed() {
        this.outboxStatus = OutboxStatus.FAILED;
    }
}
