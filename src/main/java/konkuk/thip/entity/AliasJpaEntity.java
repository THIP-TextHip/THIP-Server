package konkuk.thip.entity;

import jakarta.persistence.*;
import konkuk.thip.global.entity.BaseJpaEntity;
import lombok.*;

import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "aliases")
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

    @OneToOne(mappedBy = "aliasJpaEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private UserJpaEntity userJpaEntity;

    @OneToOne(mappedBy = "aliasJpaEntity",cascade = CascadeType.ALL, orphanRemoval = true)
    private CategoryJpaEntity categoryJpaEntity;


}
