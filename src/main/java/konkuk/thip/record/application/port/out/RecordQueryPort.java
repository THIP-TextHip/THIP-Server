package konkuk.thip.record.application.port.out;

import konkuk.thip.record.application.port.in.dto.RecordSearchResult;

public interface RecordQueryPort {

     RecordSearchResult findRecordsByRoom(Long roomId, String type, Integer pageStart, Integer pageEnd, Long userId, Integer pageNum);

}
