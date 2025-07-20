package konkuk.thip.book.domain;

import konkuk.thip.common.exception.InvalidStateException;
import lombok.Getter;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static konkuk.thip.common.exception.code.ErrorCode.*;

@Getter
public class SavedBooks {
    private final Set<Book> books;

    public SavedBooks(List<Book> books) {
        // Set으로 변환해서 중복 여부 검사
        Set<Book> bookSet = new HashSet<>(books);
        if (bookSet.size() != books.size()) {
            throw new InvalidStateException(DUPLICATED_BOOKS_IN_COLLECTION);
        }
        // 불변 Set으로 저장 (Collections.unmodifiableSet 사용)
        this.books = Collections.unmodifiableSet(bookSet);
    }

    // 중복 저장 검증
    public void validateNotAlreadySaved(Book book) {
        if (books.contains(book)) {
            throw new InvalidStateException(BOOK_ALREADY_SAVED);
        }
    }

    // 삭제 가능 여부 검증
    public void validateCanDelete(Book book) {
        if (!books.contains(book)) {
            throw new InvalidStateException(BOOK_NOT_SAVED_CANNOT_DELETE);
        }
    }
}

