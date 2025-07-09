package konkuk.thip.room.domain;

import konkuk.thip.room.adapter.out.persistence.CategoryName;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Category {

    private final CategoryName categoryName;

    public static Category from(String value) {
        return new Category(CategoryName.from(value));
    }
}
