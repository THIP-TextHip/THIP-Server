package konkuk.thip.room.adapter.out.jpa;

import com.google.common.annotations.VisibleForTesting;
import jakarta.persistence.*;
import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.common.entity.BaseJpaEntity;
import konkuk.thip.room.domain.value.Category;
import konkuk.thip.room.domain.Room;
import lombok.*;

import java.time.LocalDate;

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

    @Builder.Default
    @Column(name = "member_count",nullable = false)
    private int memberCount = 1;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private BookJpaEntity bookJpaEntity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    public RoomJpaEntity updateFrom(Room room) {
        this.title = room.getTitle();
        this.description = room.getDescription();
        this.isPublic = room.isPublic();
        this.password = room.getHashedPassword();
        this.roomPercentage = room.getRoomPercentage();
        this.startDate = room.getStartDate();
        this.endDate = room.getEndDate();
        this.recruitCount = room.getRecruitCount();
        this.memberCount = room.getMemberCount();
        return this;
    }

    @VisibleForTesting
    public void updateMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    @VisibleForTesting
    public void updateStartDate(LocalDate localDate) {
        this.startDate = localDate;
    }

    @VisibleForTesting
    public void updateIsPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    @VisibleForTesting
    public void updateRoomPercentage(double roomPercentage) {this.roomPercentage = roomPercentage;}
}
