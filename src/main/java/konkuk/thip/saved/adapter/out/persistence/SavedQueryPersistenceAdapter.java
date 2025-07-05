package konkuk.thip.saved.adapter.out.persistence;

import konkuk.thip.book.adapter.out.mapper.BookMapper;
import konkuk.thip.book.domain.Book;
import konkuk.thip.common.exception.EntityNotFoundException;
import konkuk.thip.saved.adapter.out.jpa.SavedBookJpaEntity;
import konkuk.thip.saved.adapter.out.mapper.SavedBookMapper;
import konkuk.thip.saved.adapter.out.mapper.SavedFeedMapper;
import konkuk.thip.saved.application.port.out.SavedQueryPort;
import konkuk.thip.book.domain.SavedBooks;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

import static konkuk.thip.common.exception.code.ErrorCode.USER_NOT_FOUND;

@Repository
@RequiredArgsConstructor
public class SavedQueryPersistenceAdapter implements SavedQueryPort {

    private final SavedBookJpaRepository savedBookJpaRepository;
    private final SavedFeedJpaRepository savedFeedJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final SavedBookMapper savedBookMapper;
    private final BookMapper bookMapper;
    private final SavedFeedMapper savedFeedMapper;

    @Override
    public boolean existsByUserIdAndBookId(Long userId, Long bookId) {
        return savedBookJpaRepository.existsByUserJpaEntity_UserIdAndBookJpaEntity_BookId(userId, bookId);
    }

    @Override
    public SavedBooks findByUserId(Long userId) {

        UserJpaEntity user = userJpaRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND));
        List<SavedBookJpaEntity> savedBookEntities = savedBookJpaRepository.findByUserJpaEntity_UserId(user.getUserId());

        // SavedBookJpaEntity에서 BookJpaEntity를 꺼내 도메인 Book으로 변환
        List<Book> books = savedBookEntities.stream()
                .map(entity -> bookMapper.toDomainEntity(entity.getBookJpaEntity()))
                .collect(Collectors.toList());

        return new SavedBooks(books);
    }


}
