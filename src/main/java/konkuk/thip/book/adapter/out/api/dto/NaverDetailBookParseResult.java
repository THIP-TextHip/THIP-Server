
package konkuk.thip.book.adapter.out.api.dto;

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
}