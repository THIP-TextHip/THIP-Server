package konkuk.thip.room.adapter.out.persistence.projection;

public interface RoomAggregateProjection {
    Long getRoomId();
    Double getAvgPercentage();
    Long getMemberCount();
}