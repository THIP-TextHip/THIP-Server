package konkuk.thip.book.domain;

import konkuk.thip.common.entity.BaseDomainEntity;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

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

}
