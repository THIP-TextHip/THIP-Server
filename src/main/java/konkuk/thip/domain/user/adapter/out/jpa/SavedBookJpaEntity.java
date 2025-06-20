package konkuk.thip.domain.user.adapter.out.jpa;

import jakarta.persistence.*;
import konkuk.thip.domain.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.global.entity.BaseJpaEntity;
import lombok.*;

@Entity
@Table(name = "saved_books")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class SavedBookJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long savedId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserJpaEntity userJpaEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private BookJpaEntity bookJpaEntity;
}