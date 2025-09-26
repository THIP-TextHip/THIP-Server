package konkuk.thip.notification.domain.value;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import konkuk.thip.common.exception.InvalidStateException;

import java.io.IOException;

import static konkuk.thip.common.exception.code.ErrorCode.NOTIFICATION_REDIRECT_DATA_DESERIALIZE_FAILED;
import static konkuk.thip.common.exception.code.ErrorCode.NOTIFICATION_REDIRECT_DATA_SERIALIZE_FAILED;

@Converter
public class NotificationRedirectSpecConverter implements AttributeConverter<NotificationRedirectSpec, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(NotificationRedirectSpec attribute) {
        if (attribute == null) return null;
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new InvalidStateException(NOTIFICATION_REDIRECT_DATA_SERIALIZE_FAILED);
        }
    }

    @Override
    public NotificationRedirectSpec convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) return NotificationRedirectSpec.none();
        try {
            return objectMapper.readValue(dbData, NotificationRedirectSpec.class);
        } catch (IOException e) {
            throw new InvalidStateException(NOTIFICATION_REDIRECT_DATA_DESERIALIZE_FAILED);
        }
    }
}
