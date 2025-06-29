package konkuk.thip.book.application.service;

import konkuk.thip.book.adapter.out.api.dto.NaverBookParseResult;
import konkuk.thip.book.adapter.out.api.dto.NaverDetailBookParseResult;
import konkuk.thip.book.application.port.in.BookSearchUseCase;
import konkuk.thip.book.application.port.in.dto.BookDetailSearchResult;
import konkuk.thip.book.application.port.out.BookCommandPort;
import konkuk.thip.book.application.port.out.BookApiQueryPort;
import konkuk.thip.book.domain.Book;
import konkuk.thip.common.exception.BusinessException;
import konkuk.thip.feed.application.port.out.FeedQueryPort;
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

@Service
@RequiredArgsConstructor
public class BookSearchService implements BookSearchUseCase {

    private final BookApiQueryPort bookApiQueryPort;
    private final RoomQueryPort roomQueryPort;
    private final UserQueryPort userQueryPort;
    private final FeedQueryPort feedQueryPort;
    private final SavedQueryPort savedQueryPort;
    private final BookCommandPort bookCommandPort;
    private final UserCommandPort userCommandPort;


    @Override
    public NaverBookParseResult searchBooks(String keyword, int page) {

        if (keyword == null || keyword.isBlank()) {
            throw new BusinessException(BOOK_KEYWORD_REQUIRED);
        }

        if (page < 1) {
            throw new BusinessException(BOOK_PAGE_NUMBER_INVALID);
        }

        //유저의 최근검색어 로직 추가

        int start = (page - 1) * PAGE_SIZE + 1; //검색 시작 위치
        NaverBookParseResult result = bookApiQueryPort.findBooksByKeyword(keyword, start);

        int totalElements = result.total();
        int totalPages = (totalElements + PAGE_SIZE - 1) / PAGE_SIZE;
        if ( totalElements!=0 && page > totalPages) {
            throw new BusinessException(BOOK_SEARCH_PAGE_OUT_OF_RANGE);
        }

        return result;
    }

    @Override
    public BookDetailSearchResult searchDetailBooks(String isbn) {

        //유저정보찾기
        User user =  userCommandPort.findById(2L);

        //책 상세정보
        NaverDetailBookParseResult naverDetailBookParseResult = bookApiQueryPort.findDetailBookByKeyword(isbn);

        //이책에 모집중인 모임방 개수
        Book book = bookCommandPort.findByIsbn(isbn);
        LocalDate today = LocalDate.now();
        //오늘 날짜 기준으로 방 활동 시작 기간이 이후인 방 찾기(모집중인 방)
        int recruitingRoomCount = roomQueryPort.countRecruitingRoomsByBookAndStartDateAfter(book.getId(), today);


        // 이책에 읽기 참여중인 사용자 수
        // 해당책으로 피드에 글 작성
        // 해당책에 대해 모임방 참여
        // 둘 중 하나라도 부합될 경우 카운트, 중복 카운트 불가

        // 이 책으로 만들어진 방에 참여한 사용자 ID 집합
        Set<Long> roomParticipantUserIds = userQueryPort.findUserIdsParticipatedInRoomsByBookId(book.getId());

        // 이 책으로 피드를 쓴 사용자 ID 집합
        Set<Long> feedAuthorUserIds = feedQueryPort.findUserIdsByBookId(book.getId());

        // 합집합(중복 제거)
        Set<Long> combinedUsers = new HashSet<>();
        combinedUsers.addAll(roomParticipantUserIds);
        combinedUsers.addAll(feedAuthorUserIds);

        int recruitingReadCount = combinedUsers.size();

        // 사용자의 해당 책 저장 여부
        boolean isSaved = savedQueryPort.existsByUserIdAndBookId(user.getId(), book.getId());

        return BookDetailSearchResult.of(
                naverDetailBookParseResult,
                recruitingRoomCount,
                recruitingReadCount,
                isSaved);
    }

}