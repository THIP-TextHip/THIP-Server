package konkuk.thip.room.adapter.out.jpa;

import com.google.common.annotations.VisibleForTesting;
import jakarta.persistence.*;
import konkuk.thip.attendancecheck.adapter.out.jpa.AttendanceCheckJpaEntity;
import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.common.entity.BaseJpaEntity;
import konkuk.thip.record.adapter.out.jpa.RecordJpaEntity;
import konkuk.thip.room.domain.Room;
import konkuk.thip.vote.adapter.out.jpa.VoteJpaEntity;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private BookJpaEntity bookJpaEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryJpaEntity categoryJpaEntity;

    // 삭제용 방 참여자 양방향 매핑 관계
    @OneToMany(mappedBy = "roomJpaEntity", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<RoomParticipantJpaEntity> roomParticipants = new ArrayList<>();

    // 삭제용 방 출석체크 양방향 매핑 관계
    @OneToMany(mappedBy = "roomJpaEntity", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<AttendanceCheckJpaEntity> attendanceChecks = new ArrayList<>();

    // 삭제용 투표 양방향 매핑 관계
    @OneToMany(mappedBy = "roomJpaEntity", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<VoteJpaEntity> votes = new ArrayList<>();

    // 삭제용 기록 양방향 매핑 관계
    @OneToMany(mappedBy = "roomJpaEntity", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<RecordJpaEntity> records = new ArrayList<>();

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
}
