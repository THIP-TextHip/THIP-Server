package konkuk.thip.book.application.port.out.dto;

import com.querydsl.core.annotations.QueryProjection;

import javax.annotation.Nullable;
import java.time.LocalDateTime;

public record BookQueryDto(
        Long bookId,
        String bookTitle,
        String authorName,
        String publisher,
        String bookImageUrl,
        String isbn,
        @Nullable LocalDateTime savedCreatedAt,
        @Nullable Double roomPercentage
)
{
    // 저장한 책 조회시 활용
    @QueryProjection
    public BookQueryDto(
            Long bookId,
            String bookTitle,
            String authorName,
            String publisher,
            String bookImageUrl,
            String isbn,
            LocalDateTime savedCreatedAt
    ){
        this(bookId, bookTitle, authorName, publisher, bookImageUrl, isbn, savedCreatedAt,null);
    }

    // 활동중인 모임방 책 조회시 활용
    @QueryProjection
    public BookQueryDto(
            Long bookId,
            String bookTitle,
            String authorName,
            String publisher,
            String bookImageUrl,
            String isbn,
            Double roomPercentage
    ){
        this(bookId, bookTitle, authorName, publisher, bookImageUrl, isbn, null, roomPercentage);
    }
}
