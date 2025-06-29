package konkuk.thip.book.adapter.in.web.response;

import konkuk.thip.book.adapter.out.api.dto.NaverBookParseResult;
import lombok.Builder;

import java.util.List;

import static konkuk.thip.book.adapter.out.api.NaverApiUtil.PAGE_SIZE;

@Builder
public record GetBookSearchListResponse(
        List<BookDto> searchResult, // 책 목록
        int page,                   // 현재 페이지 (1부터 시작)
        int size,                   // 한 페이지에 포함되는 데이터 수 (페이지 크기)
        long totalElements,         // 전체 데이터 개수
        int totalPages,             // 전체 페이지 수
        boolean last,               // 마지막 페이지 여부
        boolean first               // 첫 페이지 여부
) {
    public static GetBookSearchListResponse of(NaverBookParseResult result, int page) {
        int totalElements = result.total();
        int totalPages = (int) Math.ceil((double) totalElements / PAGE_SIZE);
        boolean last = (page >= totalPages);
        boolean first = (page == 1);

        List<BookDto> bookDtos = result.naverBooks().stream()
                .map(BookDto::of)
                .toList();

        return new GetBookSearchListResponse(
                bookDtos,
                page,
                PAGE_SIZE,
                totalElements,
                totalPages,
                last,
                first
        );
    }
    public record BookDto(
            String title,
            String imageUrl,
            String authorName,
            String publisher,
            String isbn
    ) {
        public static BookDto of(NaverBookParseResult.NaverBook naverBook) {
            return new BookDto(
                    naverBook.title(),
                    naverBook.imageUrl(),
                    naverBook.author(),
                    naverBook.publisher(),
                    naverBook.isbn()
            );
        }
    }
}
