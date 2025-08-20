package konkuk.thip.feed.adapter.out.jpa.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import konkuk.thip.feed.domain.value.ContentList;

import java.io.IOException;
import java.util.List;

@Converter(autoApply = false)
public class ContentListJsonConverter implements AttributeConverter<ContentList, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(ContentList attribute) {
        try {
            List<String> list = attribute == null ? List.of() : attribute.toUnmodifiableList();
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("ContentList 직렬화 실패", e);
        }
    }

    @Override
    public ContentList convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return ContentList.empty();
        }
        try {
            List<String> list = objectMapper.readValue(
                    dbData,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
            );
            return ContentList.of(list);
        } catch (IOException e) {
            throw new IllegalStateException("ContentList 역직렬화 실패", e);
        }
    }
}