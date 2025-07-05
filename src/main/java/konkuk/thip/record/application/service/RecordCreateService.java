package konkuk.thip.record.application.service;

import jakarta.transaction.Transactional;
import konkuk.thip.record.application.port.in.RecordCreateUseCase;
import konkuk.thip.record.application.port.in.dto.RecordCreateCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecordCreateService implements RecordCreateUseCase {

    @Transactional
    @Override
    public Long createRecord(RecordCreateCommand command) {
        return null;
    }
}
