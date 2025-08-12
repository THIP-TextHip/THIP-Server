package konkuk.thip.book.application.port.in.dto;

import konkuk.thip.book.adapter.out.api.dto.NaverDetailBookParseResult;


public record BookDetailSearchResult(
        NaverDetailBookParseResult naverDetailBook,
        int recruitingRoomCount,
        int readCount,
        boolean isSaved

)
{
    public static BookDetailSearchResult of(NaverDetailBookParseResult naverDetailBook ,
                                                 int recruitingRoomCount,
                                                 int readCount,
                                                 boolean isSaved) {
        return new BookDetailSearchResult(
                naverDetailBook,
                recruitingRoomCount,
                readCount,
                isSaved);
    }
}
