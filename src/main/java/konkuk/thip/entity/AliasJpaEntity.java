package konkuk.thip.entity;

import jakarta.persistence.*;
import konkuk.thip.global.entity.BaseJpaEntity;
import lombok.*;

import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "alias")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AliasJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "alias_id")
    private Long aliasId;

    @Column(name = "alias_value",length = 50, nullable = false)
    private String value;

    @OneToOne(mappedBy = "aliasJpaEntity")
    private UserJpaEntity userJpaEntity;

    @OneToMany(mappedBy = "aliasJpaEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CategoryJpaEntity> categories = new ArrayList<>();


}
