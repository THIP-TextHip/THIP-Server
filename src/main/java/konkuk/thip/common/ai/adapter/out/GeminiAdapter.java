package konkuk.thip.common.ai.adapter.out;

import konkuk.thip.book.domain.Book;
import konkuk.thip.common.ai.application.out.GeminiLoadPort;
import konkuk.thip.common.exception.InternalServerException;
import konkuk.thip.common.exception.code.ErrorCode;
import konkuk.thip.roompost.domain.Record;
import konkuk.thip.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GeminiAdapter implements GeminiLoadPort {

    private final ChatClient chatClient;

    private static final String TEMPLATE = """
        시스템: 당신은 한국어 글쓰기 튜터이자 서평가이다. 사용자의 기록을 바탕으로 논리적이고 깔끔한 독서 감상문을 작성한다.
        
        어투/스타일 규칙:
        - 시점: 1인칭(저는/제가)
        - 종결어미: 일관된 존댓말(~습니다/~합니다)
        - 문장은 간결하고 매 단락의 주제가 분명해야 함
        - 과도한 추측/단정 금지, 기록에 근거하여 주장 전개
        - 필요 시 (p.페이지)로 근거 표기
        
        책 메타데이터:
        - 책 제목: {bookTitle}
        - 책 설명: {bookDescription}
        
        사용자 정보:
        - 닉네임: {nickname}
        - 칭호: {alias}
        - 현재 누적 독후감 생성 횟수: {reviewCount}회
        
        입력 기록(페이지 오름차순, 요약/정제됨):
        {records}
        
        작성 지시:
        1) 서론-본론-결론 3단 구성으로 작성합니다.
        2) 서론: 책 설명을 바탕으로 이번 독서의 관점/목표를 간단히 밝힙니다.
        3) 본론: 기록에서 드러난 핵심 주제 2~3가지를 선택해 근거와 함께 설명합니다. 필요한 곳에 (p.xx) 표기.
        4) 결론: 독서 전후의 생각 변화/적용 계획을 간단히 정리합니다.
        5) 전체 분량은 대략 {minLen}~{maxLen}자 수준으로 맞춥니다.
        
        이제 위 정보를 바탕으로 독서 감상문을 작성하세요.
        """;

    @Override
    public String generateRecordReview(User user, List<Record> records, Book book, int minLength, int maxLength) {
        // 방어 로직: records 비어있을 수 있음 (상위 유효성에서 걸러도 한 번 더 안전장치)
        if (records == null || records.isEmpty()) {
            throw new InternalServerException(ErrorCode.GEMINI_API_REQUEST_ERROR,
                    new IllegalArgumentException("기록이 비어있습니다."));
        }

        String recordsBlock = records.stream()
                .map(r -> "- p." + r.getPage() + ": " + r.getContent())
                .collect(Collectors.joining("\n"));

        String bookTitle = book.getTitle();
        String bookDesc  = book.getDescription() == null ? "설명 없음" : book.getDescription();

        // 템플릿 렌더링
        String prompt = new PromptTemplate(TEMPLATE).render(Map.of(
                "bookTitle", bookTitle,
                "bookDescription", bookDesc,
                "nickname", user.getNickname(),
                "alias", user.getAlias().getValue(),
                "reviewCount", String.valueOf(user.getRecordReviewCount()),
                "records", recordsBlock,
                "minLen", minLength,   // 필요 시 파라미터화
                "maxLen", maxLength   // 필요 시 파라미터화
        ));

        try {
            // Spring AI ChatClient 호출
            return chatClient
                    .prompt()
                    .user(prompt)
                    .call()
                    .content();
        } catch (Exception e) {
            throw new InternalServerException(ErrorCode.GEMINI_API_RESPONSE_ERROR, e);
        }
    }
}