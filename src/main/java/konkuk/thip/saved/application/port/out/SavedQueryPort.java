package konkuk.thip.saved.application.port.out;

public interface SavedQueryPort {
    boolean existsByUserIdAndBookId(Long userId, Long bookId);
}
