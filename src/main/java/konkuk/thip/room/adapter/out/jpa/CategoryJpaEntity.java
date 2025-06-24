package konkuk.thip.room.adapter.out.jpa;

import jakarta.persistence.*;
import konkuk.thip.user.adapter.out.jpa.AliasJpaEntity;
import konkuk.thip.common.entity.BaseJpaEntity;
import lombok.*;


@Entity
@Table(name = "categories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CategoryJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "category_value",length = 50, nullable = false)
    private String value;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_alias_id", nullable = false)
    private AliasJpaEntity aliasForCategoryJpaEntity;
    
}
