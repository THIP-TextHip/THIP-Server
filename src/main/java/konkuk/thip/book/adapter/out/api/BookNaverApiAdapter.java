package konkuk.thip.book.adapter.out.api;

import konkuk.thip.book.adapter.out.api.dto.NaverBookParseResult;
import konkuk.thip.book.adapter.out.api.dto.NaverDetailBookParseResult;
import konkuk.thip.book.application.port.out.SearchDetailBookQueryPort;
import konkuk.thip.book.application.port.out.SearchBookQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookNaverApiAdapter implements SearchBookQueryPort, SearchDetailBookQueryPort {

    private final NaverApiUtil naverApiUtil;

    @Override
    public NaverBookParseResult findBooksByKeyword(String keyword, int start) {
        String xml = naverApiUtil.searchBook(keyword, start); // 네이버 API 호출
        return NaverBookXmlParser.parseBookList(xml); // XML 파싱 + 페이징 정보 포함
    }

    @Override
    public NaverDetailBookParseResult findDetailBookByKeyword(String isbn) {
        String xml = naverApiUtil.detailSearchBook(isbn); // 네이버 API 호출
        return NaverBookXmlParser.parseBookDetail(xml); // XML 파싱
    }

}