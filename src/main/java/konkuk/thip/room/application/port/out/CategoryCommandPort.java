package konkuk.thip.room.application.port.out;

import konkuk.thip.room.domain.Category;

public interface CategoryCommandPort {

    Category findByValue(String value);
}
