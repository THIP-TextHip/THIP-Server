package konkuk.thip.book.adapter.out.api.aladin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import konkuk.thip.book.adapter.out.api.dto.BookSearchResult;
import konkuk.thip.book.adapter.out.api.dto.BookDetailResult;
import konkuk.thip.common.exception.ExternalApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static konkuk.thip.book.adapter.out.api.aladin.AladinApiParam.*;
import static konkuk.thip.book.adapter.out.api.dto.BookSearchResult.PAGE_SIZE;
import static konkuk.thip.common.exception.code.ErrorCode.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class AladinApiUtil {

    // 알라딘 정책상 한 검색어당 실제로 조회 가능한 결과는 최대 200건까지로 제한된다.
    private static final int MAX_TOTAL_RESULTS = 200;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${aladin.ttbKey}")
    private String ttbKey;

    @Value("${aladin.baseUrl}")
    private String baseUrl;

    @Value("${aladin.searchUrl}")
    private String searchUrl;

    public record AladinDetailResult(BookDetailResult detail, Integer pageCount) {}

    private String buildLookupUrl(String isbn) {
        return String.format(
                baseUrl + "ttbkey=%s&itemIdType=%s&itemId=%s&output=%s&Version=%s",
                ttbKey,
                ITEM_ID_TYPE.getValue(),
                isbn,
                OUTPUT.getValue(),
                API_VERSION.getValue()
        );
    }

    // Query 값은 RestTemplate가 URI 템플릿 변수로 한 번만 인코딩하도록 {query} 플레이스홀더로 남겨둔다.
    // 여기서 미리 URLEncoder로 인코딩해버리면 RestTemplate이 다시 인코딩(이중 인코딩)하여 키워드가 깨진다.
    private String buildSearchUrlTemplate(int page) {
        return String.format(
                searchUrl + "ttbkey=%s&Query={query}&QueryType=%s&SearchTarget=%s&MaxResults=%s&start=%d&output=%s&Version=%s",
                ttbKey,
                QUERY_TYPE.getValue(),
                SEARCH_TARGET.getValue(),
                PAGE_SIZE,
                page,
                OUTPUT.getValue(),
                API_VERSION.getValue()
        );
    }

    public Integer getPageCount(String isbn) {
        return fetchDetail(isbn).pageCount();
    }

    public BookDetailResult getBookDetail(String isbn) {
        return fetchDetail(isbn).detail();
    }

    public AladinDetailResult getBookDetailWithPageCount(String isbn) {
        return fetchDetail(isbn);
    }

    private AladinDetailResult fetchDetail(String isbn) {
        String url = buildLookupUrl(isbn);
        String response = restTemplate.getForObject(url, String.class);

        try {
            JsonNode jsonNode = objectMapper.readTree(response);
            JsonNode items = jsonNode.path("item");

            // json 응답 결과에 item 키값이 없는 경우
            // TODO : 알라딘으로부터 page 정보가 없으면 ??
            // 보상 시나리오 : 유저에게 "page 정보를 찾을 수 없는 책입니다. 직접 page 정보를 입력하세요" 라고 안내
            // 일단 지금은 exception throw 만 진행
            if (!items.isArray() || items.isEmpty()) {
                throw new ExternalApiException(BOOK_ALADIN_API_ISBN_NOT_FOUND);
            }

            JsonNode item = items.get(0);
            JsonNode subInfo = item.path(SUB_INFO_PARSING_KEY.getValue());
            int pageCount = subInfo.path(PAGE_COUNT_PARSING_KEY.getValue()).asInt();

            BookDetailResult detail = BookDetailResult.builder()
                    .title(item.path("title").asText())
                    .imageUrl(item.path("cover").asText())
                    .author(item.path("author").asText())
                    .publisher(item.path("publisher").asText())
                    .isbn(item.path("isbn13").asText())
                    .description(item.path("description").asText())
                    .build();

            return new AladinDetailResult(detail, pageCount);
        } catch (IOException e) {
            throw new ExternalApiException(BOOK_ALADIN_API_PARSING_ERROR);
        }
    }

    public BookSearchResult searchBooks(String keyword, int start) {
        // BookSearchService가 넘기는 start는 (page-1)*PAGE_SIZE+1 형태의 아이템 오프셋이므로
        // 알라딘이 요구하는 "페이지 번호"로 역산한다.
        int page = ((start - 1) / PAGE_SIZE) + 1;
        String urlTemplate = buildSearchUrlTemplate(page);
        String response = restTemplate.getForObject(urlTemplate, String.class, keyword);

        try {
            JsonNode jsonNode = objectMapper.readTree(response);
            // totalResults는 전체 매칭 건수를 그대로 보고하지만 실제로 조회 가능한 건 MAX_TOTAL_RESULTS까지뿐이므로,
            // 페이지네이션(totalPages/last)이 정확히 그 지점에서 끝나도록 캡핑한다.
            int total = Math.min(jsonNode.path("totalResults").asInt(), MAX_TOTAL_RESULTS);
            int startIndex = jsonNode.path("startIndex").asInt();

            List<BookSearchResult.BookSummary> books = new ArrayList<>();
            JsonNode items = jsonNode.path("item");
            if (items.isArray()) {
                for (JsonNode item : items) {
                    books.add(BookSearchResult.BookSummary.builder()
                            .title(item.path("title").asText())
                            .imageUrl(item.path("cover").asText())
                            .author(item.path("author").asText())
                            .publisher(item.path("publisher").asText())
                            .isbn(item.path("isbn13").asText())
                            .build());
                }
            }

            return BookSearchResult.of(books, total, startIndex);
        } catch (IOException e) {
            throw new ExternalApiException(BOOK_ALADIN_API_PARSING_ERROR);
        }
    }
}
