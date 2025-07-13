package konkuk.thip.room.adapter.out.jpa;

import jakarta.persistence.*;
import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.common.entity.BaseJpaEntity;
import lombok.*;

import java.time.LocalDate;

//TODO 방에 이방에 참여중인 인원수 추가
@Entity
@Table(name = "rooms")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RoomJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roomId;

    @Column(length = 50, nullable = false)
    private String title;

    @Column(length = 230, nullable = false)
    private String description;

    @Column(name = "is_public", nullable = false)
    private boolean isPublic;

    private String password;

    @Builder.Default
    @Column(name = "room_percentage",nullable = false)
    private double roomPercentage = 0.0;

    @Column(name = "start_date",nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date",nullable = false)
    private LocalDate endDate;

    @Column(name = "recruit_count",nullable = false)
    private int recruitCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private BookJpaEntity bookJpaEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryJpaEntity categoryJpaEntity;
}