package konkuk.thip.book.adapter.out.api.aladin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AladinApiClient {

    private final AladinApiUtil aladinApiUtil;

    public Integer findPageCountByIsb(String isbn) {
        return aladinApiUtil.getPageCount(isbn);
    }
}
