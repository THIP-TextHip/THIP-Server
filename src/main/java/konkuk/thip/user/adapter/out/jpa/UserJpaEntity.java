
package konkuk.thip.user.adapter.out.jpa;

import jakarta.persistence.*;
import konkuk.thip.attendancecheck.adapter.out.jpa.AttendanceCheckJpaEntity;
import konkuk.thip.book.adapter.out.jpa.SavedBookJpaEntity;
import konkuk.thip.comment.adapter.out.jpa.CommentJpaEntity;
import konkuk.thip.comment.adapter.out.jpa.CommentLikeJpaEntity;
import konkuk.thip.common.entity.BaseJpaEntity;
import konkuk.thip.feed.adapter.out.jpa.SavedFeedJpaEntity;
import konkuk.thip.notification.adapter.out.jpa.NotificationJpaEntity;
import konkuk.thip.post.adapter.out.jpa.PostJpaEntity;
import konkuk.thip.post.adapter.out.jpa.PostLikeJpaEntity;
import konkuk.thip.recentSearch.adapter.out.jpa.RecentSearchJpaEntity;
import konkuk.thip.room.adapter.out.jpa.RoomParticipantJpaEntity;
import konkuk.thip.user.domain.User;
import konkuk.thip.vote.adapter.out.jpa.VoteParticipantJpaEntity;
import lombok.*;
import org.hibernate.annotations.SQLDelete;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE users SET status = 'INACTIVE' WHERE user_id = ?")
public class UserJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(length = 60, nullable = false)
    private String nickname;

    @Column(name = "nickname_updated_at", nullable = false)
    private LocalDate nicknameUpdatedAt; // 날짜 형식으로 저장 (예: "2023-10-01")

    @Column(name = "oauth2_id", length = 50, nullable = false)
    private String oauth2Id;

    @Builder.Default
    private Integer followerCount = 0; // 팔로워 수

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_alias_id", nullable = false)
    private AliasJpaEntity aliasForUserJpaEntity;

    // 삭제용 방 출석체크 양방향 매핑 관계
    @OneToMany(mappedBy = "userJpaEntity", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<AttendanceCheckJpaEntity> attendanceChecks = new ArrayList<>();

    // 삭제용 댓글 양방향 매핑 관계
    @OneToMany(mappedBy = "userJpaEntity", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<CommentJpaEntity> comments = new ArrayList<>(); // 사용자가 댓글 남긴 게시글의 댓글 수 감소

    // 삭제용 댓글 좋아요 양방향 매핑 관계
    @OneToMany(mappedBy = "userJpaEntity", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<CommentLikeJpaEntity> commentLikes = new ArrayList<>(); // 사용자가 좋아요 남긴 댓글의 좋아요 수 감소

    // 삭제용 게시글 양방향 매핑 관계
    @OneToMany(mappedBy = "userJpaEntity", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<PostJpaEntity> posts = new ArrayList<>();

    // 삭제용 게시글 좋아요 양방향 매핑 관계
    @OneToMany(mappedBy = "userJpaEntity", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<PostLikeJpaEntity> postLikes = new ArrayList<>(); // 사용자가 좋아요 남긴 게시글의 좋아요 수 감소

    // 삭제용 알림 양방향 매핑 관계
    @OneToMany(mappedBy = "userJpaEntity", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<NotificationJpaEntity> notifications = new ArrayList<>();

    // 삭제용 최근 검색어 양방향 매핑 관계
    @OneToMany(mappedBy = "userJpaEntity", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<RecentSearchJpaEntity> recentSearches = new ArrayList<>();

    // 삭제용 투표 참여자 양방향 매핑 관계
    @OneToMany(mappedBy = "userJpaEntity", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<VoteParticipantJpaEntity> voteParticipants = new ArrayList<>(); // 사용자가 투표한 항목의 투표 수 감소

    // 삭제용 방 참여자 양방향 매핑 관계
    @OneToMany(mappedBy = "userJpaEntity", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<RoomParticipantJpaEntity> roomParticipants = new ArrayList<>();
    // 사용자가 참여한 방의 멤버 수 감소
    // 사용자가 방의 HOST라면 연관된 방 삭제

    // 삭제용 피드 저장 양방향 매핑 관계
    @OneToMany(mappedBy = "userJpaEntity", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<SavedFeedJpaEntity> savedFeeds = new ArrayList<>();

    // 삭제용 책 저장 양방향 매핑 관계
    @OneToMany(mappedBy = "userJpaEntity", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<SavedBookJpaEntity> savedBooks = new ArrayList<>();


    public void updateIncludeAliasFrom(User user, AliasJpaEntity aliasJpaEntity) {
        this.nickname = user.getNickname();
        this.nicknameUpdatedAt = user.getNicknameUpdatedAt();
        this.role = UserRole.from(user.getUserRole());
        this.followerCount = user.getFollowerCount();
        this.aliasForUserJpaEntity = aliasJpaEntity;
    }

    public void updateFrom(User user) {
        this.nickname = user.getNickname();
        this.nicknameUpdatedAt = user.getNicknameUpdatedAt();
        this.role = UserRole.from(user.getUserRole());
        this.followerCount = user.getFollowerCount();
    }
}
