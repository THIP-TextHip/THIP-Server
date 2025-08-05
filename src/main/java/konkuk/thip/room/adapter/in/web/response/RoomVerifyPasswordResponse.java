package konkuk.thip.room.adapter.in.web.response;

public record RoomVerifyPasswordResponse(
        boolean matched,
        Long roomId
)
{
    public static RoomVerifyPasswordResponse of(boolean matched,Long roomId) {
        return new RoomVerifyPasswordResponse(matched,roomId);
    }
}
