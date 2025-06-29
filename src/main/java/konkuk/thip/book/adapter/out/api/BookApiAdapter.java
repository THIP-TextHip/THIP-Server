package konkuk.thip.book.adapter.out.api;

import konkuk.thip.book.adapter.out.api.dto.NaverBookParseResult;
import konkuk.thip.book.application.port.out.SearchBookQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookApiAdapter implements SearchBookQueryPort {

    private final NaverApiUtil naverApiUtil;

    @Override
    public NaverBookParseResult findBooksByKeyword(String keyword, int start) {
        String xml = naverApiUtil.searchBook(keyword, start); // 네이버 API 호출
        return NaverBookXmlParser.parse(xml);                // XML 파싱 + 페이징 정보 포함
    }
}