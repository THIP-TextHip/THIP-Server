package konkuk.thip.entity;

import jakarta.persistence.*;
import konkuk.thip.global.entity.BaseJpaEntity;
import lombok.*;

import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "category")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CategoryJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long categoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alias_id", nullable = false)
    private AliasJpaEntity aliasJpaEntity;

    @Column(name = "category_value",length = 50, nullable = false)
    private String value;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TagJpaEntity> tagJpaEntities = new ArrayList<>();
}
