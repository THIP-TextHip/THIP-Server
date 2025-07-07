package konkuk.thip.record.application.service;

import konkuk.thip.record.application.port.in.dto.RecordSearchQuery;
import konkuk.thip.record.application.port.in.dto.RecordSearchResult;
import konkuk.thip.record.application.port.in.dto.RecordSearchUseCase;
import org.springframework.stereotype.Service;

@Service
public class RecordSearchService implements RecordSearchUseCase {

    @Override
    public RecordSearchResult search(RecordSearchQuery query) {
        return null;
    }
}
