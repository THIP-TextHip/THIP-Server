package konkuk.thip.feed.adapter.out.jpa.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import konkuk.thip.feed.domain.value.Tag;
import konkuk.thip.feed.domain.value.TagList;

import java.io.IOException;
import java.util.List;

@Converter(autoApply = true)
public class TagListJsonConverter implements AttributeConverter<TagList, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(TagList attribute) {
        try {
            List<String> tagNames = attribute == null ? List.of() :
                    attribute.toUnmodifiableList().stream()
                            .map(Tag::name) // Enum 이름 사용
                            .toList();
            return objectMapper.writeValueAsString(tagNames);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("TagList 직렬화 실패", e);
        }
    }

    @Override
    public TagList convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) return TagList.of(List.of());
        try {
            List<String> names = objectMapper.readValue(
                    dbData,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
            );
            List<Tag> tags = names.stream().map(Tag::valueOf).toList();
            return TagList.of(tags);
        } catch (IOException e) {
            throw new IllegalStateException("TagList 역직렬화 실패", e);
        }
    }
}