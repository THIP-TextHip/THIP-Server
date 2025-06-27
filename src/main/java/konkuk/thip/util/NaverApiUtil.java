package konkuk.thip.util;

import konkuk.thip.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import static konkuk.thip.common.exception.code.ErrorCode.*;

@RequiredArgsConstructor
@Component
public class NaverApiUtil {

    @Value("${naver.clientId}")
    private String clientId;
    @Value("${naver.clientSecret}")
    private String clientSecret;

    private final String NAVER_BOOK_SEARCH_URL = "https://openapi.naver.com/v1/search/book.xml?query=";   //책 검색 결과 조회


    public String searchBook(String keyword, int start){
        String query = keywordToEncoding(keyword);
        String url = buildSearchApiUrl(query, start);

        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("X-Naver-Client-Id", clientId);
        requestHeaders.put("X-Naver-Client-Secret", clientSecret);

        return get(url,requestHeaders);
    }

    private String buildSearchApiUrl(String query,Integer start) {
        return NAVER_BOOK_SEARCH_URL+query+"&start="+start;
    }

    private String keywordToEncoding(String keyword) {
        String text = null;
        try {
            text = URLEncoder.encode(keyword, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new BusinessException(BOOK_KEYWORD_ENCODING_FAILED);
        }
        return text;
    }


    String get(String apiUrl, Map<String, String> requestHeaders){
        HttpURLConnection con = connect(apiUrl);
        try {
            con.setRequestMethod("GET");
            for(Map.Entry<String, String> header :requestHeaders.entrySet()) {
                con.setRequestProperty(header.getKey(), header.getValue());
            }

            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) { // 정상 호출
                return readBody(con.getInputStream());
            } else { // 오류 발생
                return readBody(con.getErrorStream());
            }
        } catch (IOException e) {
            throw new BusinessException(BOOK_NAVER_API_REQUEST_ERROR);
        } finally {
            con.disconnect();
        }
    }


    private HttpURLConnection connect(String apiUrl){
        try {
            URL url = new URL(apiUrl);
            return (HttpURLConnection)url.openConnection();
        } catch (MalformedURLException e) {
            throw new BusinessException(BOOK_NAVER_API_URL_ERROR);
        } catch (IOException e) {
            throw new BusinessException(BOOK_NAVER_API_URL_HTTP_CONNECT_FAILED);
        }
    }


    private String readBody(InputStream body){
        InputStreamReader streamReader = new InputStreamReader(body);

        try (BufferedReader lineReader = new BufferedReader(streamReader)) {
            StringBuilder responseBody = new StringBuilder();

            String line;
            while ((line = lineReader.readLine()) != null) {
                responseBody.append(line);
            }

            return responseBody.toString();
        } catch (IOException e) {
            throw new BusinessException(BOOK_NAVER_API_RESPONSE_ERROR);
        }
    }

}
