package konkuk.thip.book.adapter.out.api.aladin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import konkuk.thip.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

import static konkuk.thip.book.adapter.out.api.aladin.AladinApiParam.*;
import static konkuk.thip.common.exception.code.ErrorCode.BOOK_ALADIN_API_ISBN_NOT_FOUND;
import static konkuk.thip.common.exception.code.ErrorCode.BOOK_ALADIN_API_PARSING_ERROR;

@Component
@RequiredArgsConstructor
@Slf4j
public class AladinApiUtil {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${aladin.ttbKey}")
    private String ttbKey;

    @Value("${aladin.baseUrl}")
    private String baseUrl;


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

    public Integer getPageCount(String isbn) {
        String url = buildLookupUrl(isbn);
        String response = restTemplate.getForObject(url, String.class);

        try {
            JsonNode jsonNode = objectMapper.readTree(response);
            JsonNode items = jsonNode.path("item");

            // json 응답 결과에 item 키값이 없는 경우
            if (!items.isArray() || items.isEmpty()) {
                // TODO : 알라딘으로부터 page 정보가 없으면 ??
                // 보상 시나리오 : 유저에게 "page 정보를 찾을 수 없는 책입니다. 직접 page 정보를 입력하세요" 라고 안내
                // 일단 지금은 exception throw 만 진행
                throw new BusinessException(BOOK_ALADIN_API_ISBN_NOT_FOUND);
            }

            JsonNode subInfo = items.get(0).path(SUB_INFO_PARSING_KEY.getValue());

            return subInfo.path(PAGE_COUNT_PARSING_KEY.getValue()).asInt();
        } catch (IOException e) {
            throw new BusinessException(BOOK_ALADIN_API_PARSING_ERROR);
        }
    }
}
