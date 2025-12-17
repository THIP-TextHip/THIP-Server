package konkuk.thip.common.ai.application.out;

import konkuk.thip.book.domain.Book;
import konkuk.thip.roompost.domain.Record;
import konkuk.thip.user.domain.User;

import java.util.List;

public interface GeminiLoadPort {
    String generateRecordReview(User user, List<Record> records, Book book, int minLength, int maxLength);
}
