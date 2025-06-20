package konkuk.thip.domain.user.adapter.out.jpa;

import jakarta.persistence.*;
import konkuk.thip.global.entity.BaseJpaEntity;
import lombok.*;


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

}
