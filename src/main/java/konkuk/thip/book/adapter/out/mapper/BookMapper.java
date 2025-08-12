package konkuk.thip.book.adapter.out.mapper;

import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.domain.Book;
import org.springframework.stereotype.Component;

@Component
public class BookMapper {

    public BookJpaEntity toJpaEntity(Book book) {
        return BookJpaEntity.builder()
                .title(book.getTitle())
                .isbn(book.getIsbn())
                .authorName(book.getAuthorName())
                .bestSeller(book.isBestSeller())
                .publisher(book.getPublisher())
                .imageUrl(book.getImageUrl())
                .pageCount(book.getPageCount())
                .description(book.getDescription())
                .build();
    }

    public Book toDomainEntity(BookJpaEntity bookJpaEntity) {
        return Book.builder()
                .id(bookJpaEntity.getBookId())
                .title(bookJpaEntity.getTitle())
                .isbn(bookJpaEntity.getIsbn())
                .authorName(bookJpaEntity.getAuthorName())
                .bestSeller(bookJpaEntity.isBestSeller())
                .publisher(bookJpaEntity.getPublisher())
                .imageUrl(bookJpaEntity.getImageUrl())
                .pageCount(bookJpaEntity.getPageCount())
                .description(bookJpaEntity.getDescription())
                .createdAt(bookJpaEntity.getCreatedAt())
                .modifiedAt(bookJpaEntity.getModifiedAt())
                .status(bookJpaEntity.getStatus())
                .build();
    }
}
