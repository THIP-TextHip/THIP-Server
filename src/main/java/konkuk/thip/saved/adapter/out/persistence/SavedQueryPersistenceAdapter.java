package konkuk.thip.saved.adapter.out.persistence;

import konkuk.thip.saved.adapter.out.mapper.SavedBookMapper;
import konkuk.thip.saved.adapter.out.mapper.SavedFeedMapper;
import konkuk.thip.saved.application.port.out.SavedQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SavedQueryPersistenceAdapter implements SavedQueryPort {

    private final SavedBookRepository savedBookRepository;
    private final SavedFeedRepository savedFeedRepository;
    private final SavedBookMapper savedBookMapper;
    private final SavedFeedMapper savedFeedMapper;

}