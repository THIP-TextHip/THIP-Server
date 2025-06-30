package konkuk.thip.book.application.service;

import jakarta.transaction.Transactional;
import konkuk.thip.book.adapter.out.api.dto.NaverBookParseResult;
import konkuk.thip.book.adapter.out.api.dto.NaverDetailBookParseResult;
import konkuk.thip.book.application.port.in.BookSavedUseCase;
import konkuk.thip.book.application.port.in.BookSearchUseCase;
import konkuk.thip.book.application.port.in.dto.BookDetailSearchResult;
import konkuk.thip.book.application.port.in.dto.BookIsSavedResult;
import konkuk.thip.book.application.port.out.BookApiQueryPort;
import konkuk.thip.book.application.port.out.BookCommandPort;
import konkuk.thip.book.domain.Book;
import konkuk.thip.common.exception.BusinessException;
import konkuk.thip.feed.application.port.out.FeedQueryPort;
import konkuk.thip.recentSearch.application.port.out.RecentSearchCommandPort;
import konkuk.thip.room.application.port.out.RoomQueryPort;
import konkuk.thip.saved.application.port.out.SavedQueryPort;
import konkuk.thip.user.application.port.out.UserCommandPort;
import konkuk.thip.user.application.port.out.UserQueryPort;
import konkuk.thip.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import static konkuk.thip.book.adapter.out.api.NaverApiUtil.PAGE_SIZE;
import static konkuk.thip.common.exception.code.ErrorCode.*;
import static konkuk.thip.recentSearch.adapter.out.jpa.SearchType.BOOK_SEARCH;

@Service
@RequiredArgsConstructor
public class BookSavedService implements BookSavedUseCase {

    private final UserCommandPort userCommandPort;
    private final BookCommandPort bookCommandPort;

    @Override
    @Transactional
    public BookIsSavedResult isSavedBook(String isbn, boolean type, Long userId) {

        User user = userCommandPort.findById(userId);




        return null;
    }
}