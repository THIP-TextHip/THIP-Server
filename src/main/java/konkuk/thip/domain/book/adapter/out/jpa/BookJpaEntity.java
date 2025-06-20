package konkuk.thip.domain.book.adapter.out.jpa;

import jakarta.persistence.*;
import konkuk.thip.domain.room.adapter.out.jpa.RoomJpaEntity;
import konkuk.thip.global.entity.BaseJpaEntity;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "books")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class BookJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookId;

    @Column(length = 100, nullable = false)
    private String title;

    @Column(length = 13, nullable = false, unique = true)
    private String isbn;

    @Column(name = "author_name",length = 50, nullable = false)
    private String authorName;

    @Column(name = "best_seller",nullable = false)
    private boolean bestSeller;

    private String publisher;

    @Column(name = "image_url",columnDefinition = "TEXT", nullable = false)
    private String imageUrl;

    @Column(name = "page_count")
    private Integer pageCount;

    @Column(length = 1000)
    private String description;

    @Builder.Default
    @OneToMany(mappedBy = "bookJpaEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoomJpaEntity> roomJpaEntities = new ArrayList<>();
}