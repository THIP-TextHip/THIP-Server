package konkuk.thip.record.application.port.out;


import konkuk.thip.record.domain.Record;

public interface RecordCommandPort {

    Long saveRecord(Record record);

}
