package konkuk.thip.book.adapter.in.web.response;

import lombok.Builder;

import java.util.List;

@Builder
public record GetBookSearchListResponse(
        List<BookDto> searchResult, // 책 목록
        int page,                   // 현재 페이지 (1부터 시작)
        long totalElements,         // 전체 데이터 개수
        int totalPages,             // 전체 페이지 수
        boolean last,               // 마지막 페이지 여부
        boolean first               // 첫 페이지 여부
) {
}