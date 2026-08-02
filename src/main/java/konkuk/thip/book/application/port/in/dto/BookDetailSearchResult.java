package konkuk.thip.book.application.port.in.dto;

import konkuk.thip.book.adapter.out.api.dto.BookDetailResult;


public record BookDetailSearchResult(
        BookDetailResult bookDetail,
        int recruitingRoomCount,
        int readCount,
        boolean isSaved

)
{
    public static BookDetailSearchResult of(BookDetailResult bookDetail,
                                                 int recruitingRoomCount,
                                                 int readCount,
                                                 boolean isSaved) {
        return new BookDetailSearchResult(
                bookDetail,
                recruitingRoomCount,
                readCount,
                isSaved);
    }
}
