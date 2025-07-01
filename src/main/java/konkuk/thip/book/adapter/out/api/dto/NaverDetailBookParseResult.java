
package konkuk.thip.book.adapter.out.api.dto;

import konkuk.thip.book.domain.Book;
import lombok.Builder;

@Builder
public record NaverDetailBookParseResult(
            String title,
            String imageUrl,
            String author,
            String publisher,
            String isbn,
            String description
    ) {
    public static Book toBook(NaverDetailBookParseResult naverDetailBookParseResult) {
        return Book.builder()
                .title(naverDetailBookParseResult.title)
                .isbn(naverDetailBookParseResult.isbn)
                .authorName(naverDetailBookParseResult.author)
                .publisher(naverDetailBookParseResult.publisher)
                .bestSeller(false) //베스트셀러 구현 어떻게 할지 확정되면 나중에 수정,
                // 지금은 베스트셀러 조회안하고 무조건 저장되는 책은 베스트셀러가아닌걸로 가정
                .imageUrl(naverDetailBookParseResult.imageUrl)
                .description(naverDetailBookParseResult.description)
                .build();
    }
}