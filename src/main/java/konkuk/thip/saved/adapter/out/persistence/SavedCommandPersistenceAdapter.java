package konkuk.thip.saved.adapter.out.persistence;

import konkuk.thip.saved.adapter.out.mapper.SavedBookMapper;
import konkuk.thip.saved.adapter.out.mapper.SavedFeedMapper;
import konkuk.thip.saved.application.port.out.SavedCommandPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SavedCommandPersistenceAdapter implements SavedCommandPort {

    private final SavedBookRepository savedBookRepository;
    private final SavedFeedRepository savedFeedRepository;
    private final SavedBookMapper savedBookMapper;
    private final SavedFeedMapper savedFeedMapper;

}
