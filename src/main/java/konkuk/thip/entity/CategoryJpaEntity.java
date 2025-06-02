package konkuk.thip.entity;

import jakarta.persistence.*;
import konkuk.thip.global.entity.BaseJpaEntity;
import lombok.*;

import java.util.ArrayList;
import java.util.List;


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

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alias_id", nullable = false)
    private AliasJpaEntity aliasJpaEntity;

    @OneToMany(mappedBy = "categoryJpaEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TagJpaEntity> tagJpaEntities = new ArrayList<>();
}
