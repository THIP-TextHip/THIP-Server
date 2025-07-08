package konkuk.thip.book.adapter.out.api;

import konkuk.thip.book.adapter.out.api.dto.NaverBookParseResult;
import konkuk.thip.book.adapter.out.api.dto.NaverDetailBookParseResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NaverApiClient {

    private final NaverApiUtil naverApiUtil;

    public NaverBookParseResult findBooksByKeyword(String keyword, int start) {
        String xml = naverApiUtil.searchBook(keyword, start); // 네이버 API 호출
        return NaverBookXmlParser.parseBookList(xml); // XML 파싱 + 페이징 정보 포함
    }

    public NaverDetailBookParseResult findDetailBookByIsbn(String isbn) {
        String xml = naverApiUtil.detailSearchBook(isbn); // 네이버 API 호출
        return NaverBookXmlParser.parseBookDetail(xml); // XML 파싱
    }

}