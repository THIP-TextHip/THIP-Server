package konkuk.thip.book.application.service;

import jakarta.transaction.Transactional;
import konkuk.thip.book.adapter.out.api.dto.NaverBookParseResult;
import konkuk.thip.book.adapter.out.api.dto.NaverDetailBookParseResult;
import konkuk.thip.book.application.port.in.BookSearchUseCase;
import konkuk.thip.book.application.port.in.dto.BookDetailSearchResult;
import konkuk.thip.book.application.port.out.BookCommandPort;
import konkuk.thip.book.application.port.out.BookApiQueryPort;
import konkuk.thip.book.domain.Book;
import konkuk.thip.common.exception.BusinessException;
import konkuk.thip.feed.application.port.out.FeedQueryPort;
import konkuk.thip.recentSearch.application.port.out.RecentSearchCommandPort;
import konkuk.thip.recentSearch.domain.RecentSearch;
import konkuk.thip.room.application.port.out.RoomQueryPort;
import konkuk.thip.saved.application.port.out.SavedQueryPort;
import konkuk.thip.user.application.port.out.UserCommandPort;
import konkuk.thip.user.application.port.out.UserQueryPort;
import konkuk.thip.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static konkuk.thip.book.adapter.out.api.NaverApiUtil.PAGE_SIZE;
import static konkuk.thip.common.exception.code.ErrorCode.*;
import static konkuk.thip.recentSearch.adapter.out.jpa.SearchType.BOOK_SEARCH;

@Service
@RequiredArgsConstructor
public class BookSearchService implements BookSearchUseCase {

    private final BookApiQueryPort bookApiQueryPort;
    private final RoomQueryPort roomQueryPort;
    private final UserQueryPort userQueryPort;
    private final FeedQueryPort feedQueryPort;
    private final SavedQueryPort savedQueryPort;
    private final RecentSearchCommandPort recentSearchCommandPort;
    private final BookCommandPort bookCommandPort;
    private final UserCommandPort userCommandPort;


    @Override
    @Transactional
    public NaverBookParseResult searchBooks(String keyword, int page, Long userId) {

        if (keyword == null || keyword.isBlank()) {
            throw new BusinessException(BOOK_KEYWORD_REQUIRED);
        }

        if (page < 1) {
            throw new BusinessException(BOOK_PAGE_NUMBER_INVALID);
        }


        int start = (page - 1) * PAGE_SIZE + 1; //검색 시작 위치
        NaverBookParseResult result = bookApiQueryPort.findBooksByKeyword(keyword, start);

        int totalElements = result.total();
        int totalPages = (totalElements + PAGE_SIZE - 1) / PAGE_SIZE;
        if ( totalElements!=0 && page > totalPages) {
            throw new BusinessException(BOOK_SEARCH_PAGE_OUT_OF_RANGE);
        }

        //최근검색어 추가
        User user = userCommandPort.findById(userId);
        RecentSearch  recentSearch =  RecentSearch.builder()
                        .searchTerm(keyword)
                        .type(BOOK_SEARCH.getSearchType())
                        .userId(user.getId())
                        .build();


        recentSearchCommandPort.save(user.getId(),recentSearch);

        return result;
    }

    @Override
    public BookDetailSearchResult searchDetailBooks(String isbn,Long userId) {

        //유저정보찾기
        User user =  userCommandPort.findById(userId);

        //책 상세정보
        NaverDetailBookParseResult naverDetailBookParseResult = bookApiQueryPort.findDetailBookByKeyword(isbn);


        Optional<Book> bookOpt = bookCommandPort.findByIsbn(isbn);

        if (bookOpt.isEmpty()) {
            // 책이 없으면 기본값으로 반환
            return BookDetailSearchResult.of(
                    naverDetailBookParseResult,
                    0,
                    0,
                    false
            );
        }

        Book book = bookOpt.get();

        //이책에 모집중인 모임방 개수
        int recruitingRoomCount = getRecruitingRoomCount(book);
        // 이책에 읽기 참여중인 사용자 수
        int recruitingReadCount = getRecruitingReadCount(book);
        // 사용자의 해당 책 저장 여부
        boolean isSaved = savedQueryPort.existsByUserIdAndBookId(user.getId(), book.getId());

        return BookDetailSearchResult.of(
                naverDetailBookParseResult,
                recruitingRoomCount,
                recruitingReadCount,
                isSaved);
    }

    private int getRecruitingRoomCount(Book book) {
        //오늘 날짜 기준으로 방 활동 시작 기간이 이후인 방 찾기(모집중인 방)
        LocalDate today = LocalDate.now();
        return roomQueryPort.countRecruitingRoomsByBookAndStartDateAfter(book.getId(), today);
    }

    private int getRecruitingReadCount(Book book) {
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
        return combinedUsers.size();

    }

}