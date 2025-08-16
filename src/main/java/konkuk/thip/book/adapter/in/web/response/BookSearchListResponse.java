package konkuk.thip.book.adapter.in.web.response;

import konkuk.thip.book.adapter.out.api.dto.NaverBookParseResult;

import java.util.List;

import static konkuk.thip.book.adapter.out.api.naver.NaverApiUtil.PAGE_SIZE;

public record BookSearchListResponse(
        List<BookSearchDto> searchResult, // 책 목록
        int page,                   // 현재 페이지 (1부터 시작)
        int size,                   // 한 페이지에 포함되는 데이터 수 (페이지 크기)
        long totalElements,         // 전체 데이터 개수
        int totalPages,             // 전체 페이지 수
        boolean last,               // 마지막 페이지 여부
        boolean first               // 첫 페이지 여부
) {
    public static BookSearchListResponse of(NaverBookParseResult result, int page) {
        int totalElements = result.total();
        int totalPages = (int) Math.ceil((double) totalElements / PAGE_SIZE);
        boolean last = (page >= totalPages);
        boolean first = (page == 1);

        List<BookSearchDto> bookSearchDtos = result.naverBooks().stream()
                .map(BookSearchDto::of)
                .toList();

        return new BookSearchListResponse(
                bookSearchDtos,
                page,
                PAGE_SIZE,
                totalElements,
                totalPages,
                last,
                first
        );
    }
    public record BookSearchDto(
            String title,
            String imageUrl,
            String authorName,
            String publisher,
            String isbn
    ) {
        public static BookSearchDto of(NaverBookParseResult.NaverBook naverBook) {
            return new BookSearchDto(
                    naverBook.title(),
                    naverBook.imageUrl(),
                    naverBook.author(),
                    naverBook.publisher(),
                    naverBook.isbn()
            );
        }
    }
}
