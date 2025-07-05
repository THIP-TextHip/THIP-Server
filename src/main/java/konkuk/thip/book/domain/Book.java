package konkuk.thip.book.domain;

import konkuk.thip.common.entity.BaseDomainEntity;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.Objects;

@Getter
@SuperBuilder
public class Book extends BaseDomainEntity {

    private Long id;

    private String title;

    private String isbn;

    private String authorName;

    private boolean bestSeller;

    private String publisher;

    private String imageUrl;

    private Integer pageCount;

    private String description;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Book)) return false;
        Book book = (Book) o;
        return Objects.equals(isbn, book.isbn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isbn);
    }

    public static Book withoutId(String title, String isbn, String authorName, boolean bestSeller, String publisher, String imageUrl,Integer pageCount, String description) {
        return Book.builder()
                .id(null)
                .title(title)
                .isbn(isbn)
                .authorName(authorName)
                .bestSeller(bestSeller)
                .publisher(publisher)
                .imageUrl(imageUrl)
                .pageCount(pageCount)
                .description(description)
                .build();
    }

    public Book withId(Long id) {
        return Book.builder()
                .id(id)
                .title(this.title)
                .isbn(this.isbn)
                .authorName(this.authorName)
                .bestSeller(this.bestSeller)
                .publisher(this.publisher)
                .imageUrl(this.imageUrl)
                .pageCount(this.pageCount)
                .description(this.description)
                .build();
    }


}
