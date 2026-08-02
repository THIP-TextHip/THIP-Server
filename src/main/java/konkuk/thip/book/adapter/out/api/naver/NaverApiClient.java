package konkuk.thip.book.adapter.out.api.naver;

import konkuk.thip.book.adapter.out.api.dto.BookSearchResult;
import konkuk.thip.book.adapter.out.api.dto.BookDetailResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NaverApiClient {

    private final NaverApiUtil naverApiUtil;

    public BookSearchResult findBooksByKeyword(String keyword, int start) {
        String xml = naverApiUtil.searchBook(keyword, start); // 네이버 API 호출
        return NaverBookXmlParser.parseBookList(xml); // XML 파싱 + 페이징 정보 포함
    }

    public BookDetailResult findDetailBookByIsbn(String isbn) {
        String xml = naverApiUtil.detailSearchBook(isbn); // 네이버 API 호출
        return NaverBookXmlParser.parseBookDetail(xml); // XML 파싱
    }

}