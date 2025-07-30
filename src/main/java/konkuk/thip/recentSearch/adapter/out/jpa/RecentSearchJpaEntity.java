
package konkuk.thip.recentSearch.adapter.out.jpa;

import jakarta.persistence.*;
import konkuk.thip.common.entity.BaseJpaEntity;
import konkuk.thip.recentSearch.domain.RecentSearch;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import lombok.*;


@Entity
@Table(name = "recent_searches")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RecentSearchJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long recentSearchId;

    @Column(name = "search_term",length = 50, nullable = false)
    private String searchTerm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SearchType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserJpaEntity userJpaEntity;

    public void updateFrom(RecentSearch recentSearch) {
        this.searchTerm = recentSearch.getSearchTerm();
        this.type = SearchType.from(recentSearch.getType());
        this.setModifiedAt(recentSearch.getModifiedAt());
    }
}