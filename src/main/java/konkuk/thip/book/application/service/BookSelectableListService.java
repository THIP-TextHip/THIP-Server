package konkuk.thip.book.application.service;

import konkuk.thip.book.application.port.in.dto.BookSelectableType;
import konkuk.thip.book.adapter.in.web.response.BookSelectableListResponse;
import konkuk.thip.book.application.port.in.BookSelectableListUseCase;
import konkuk.thip.book.domain.Book;

import java.util.List;

import static konkuk.thip.book.application.port.in.dto.BookSelectableType.*;

public class BookSelectableListService implements BookSelectableListUseCase {
    @Override
    public BookSelectableListResponse getSelectableBookList(BookSelectableType bookSelectableType, Long userId) {
        List<Book> bookList = switch(bookSelectableType) {
            case SAVED -> {
                // 저장된 책 목록을 조회하는 로직
                // 예시: return bookRepository.findSavedBooksByUserId(userId);

            }
            case JOINING -> {
                // 참여 중인 모임 방의 책 목록을 조회하는 로직
                // 예시: return bookRepository.findJoiningBooksByUserId(userId);
                
            }

        }
    }
}
