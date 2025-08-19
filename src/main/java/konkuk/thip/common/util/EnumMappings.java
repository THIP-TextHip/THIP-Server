package konkuk.thip.common.util;

import konkuk.thip.feed.domain.Tag;
import konkuk.thip.room.domain.Category;
import konkuk.thip.user.domain.Alias;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class EnumMappings {

    private EnumMappings() {}

    private static final Map<Alias, Category> aliasToCategory;
    private static final Map<Category, List<Tag>> categoryToTags;

    // 역방향 인덱스
    private static final Map<Category, Alias> categoryToAlias;
    private static final Map<Tag, Category> tagToCategory;

    static {
        // Alias -> Category
        EnumMap<Alias, Category> a2c = new EnumMap<>(Alias.class);
        a2c.put(Alias.WRITER,      Category.LITERATURE);        // 문학가
        a2c.put(Alias.SCIENTIST,   Category.SCIENCE_IT);        // 과학자
        a2c.put(Alias.SOCIOLOGIST, Category.SOCIAL_SCIENCE);    // 사회학
        a2c.put(Alias.ARTIST,      Category.ART);               // 예술가
        a2c.put(Alias.PHILOSOPHER, Category.HUMANITY);          // 철학자
        aliasToCategory = Collections.unmodifiableMap(a2c);

        // Category -> Tags
        EnumMap<Category, List<Tag>> c2t = new EnumMap<>(Category.class);
        c2t.put(Category.LITERATURE, List.of(                   // 문학
                Tag.KOREAN_NOVEL, Tag.FOREIGN_NOVEL, Tag.CLASSIC_LITERATURE, Tag.ESSAY, Tag.POETRY,
                Tag.PLAY, Tag.DETECTIVE_NOVEL, Tag.FANTASY_NOVEL, Tag.ROMANCE_NOVEL, Tag.LITERARY_THEORY
        ));
        c2t.put(Category.SCIENCE_IT, List.of(                   // 과학·IT
                Tag.GENERAL_SCIENCE, Tag.PHYSICS, Tag.CHEMISTRY, Tag.BIOLOGY, Tag.ASTRONOMY,
                Tag.EARTH_SCIENCE, Tag.MATHEMATICS, Tag.GENERAL_ENGINEERING,
                Tag.COMPUTER_ENGINEERING, Tag.PROGRAMMING, Tag.IT_GENERAL
        ));
        c2t.put(Category.SOCIAL_SCIENCE, List.of(               // 사회과학
                Tag.SOCIOLOGY, Tag.LAW, Tag.GENERAL_POLITICS, Tag.POLITICAL_SCIENCE,
                Tag.ECONOMICS, Tag.BUSINESS_ADMIN, Tag.JURISPRUDENCE, Tag.EDUCATION,
                Tag.PSYCHOLOGY, Tag.MEDIA, Tag.INTERNATIONAL_RELATIONS, Tag.SOCIAL_ISSUES,
                Tag.MARKETING, Tag.INVESTMENT, Tag.STARTUP, Tag.GENERAL_ECONOMY
        ));
        c2t.put(Category.HUMANITY, List.of(                      // 인문학
                Tag.PHILOSOPHY, Tag.HISTORY, Tag.RELIGION, Tag.CLASSICS, Tag.LINGUISTICS,
                Tag.CULTURAL_ANTHROPOLOGY, Tag.HUMANISTIC_ESSAY, Tag.EASTERN_PHILOSOPHY,
                Tag.WESTERN_PHILOSOPHY, Tag.WORLD_HISTORY, Tag.KOREAN_HISTORY,
                Tag.HISTORICAL_ESSAY, Tag.ANCIENT_HISTORY, Tag.MODERN_HISTORY
        ));
        c2t.put(Category.ART, List.of(
                Tag.ART, Tag.MUSIC, Tag.MOVIE, Tag.PHOTO, Tag.DANCE,
                Tag.THEATER, Tag.DESIGN, Tag.ARCHITECTURE, Tag.GENERAL_ART
        ));
        categoryToTags = Collections.unmodifiableMap(c2t);

        // ------------역방향 인덱스------------
        // Category -> Alias
        EnumMap<Category, Alias> c2a = new EnumMap<>(Category.class);
        aliasToCategory.forEach((alias, category) -> {
            Alias prev = c2a.put(category, alias);
            if (prev != null && prev != alias) {
                throw new IllegalStateException("Category에 두 개 이상의 Alias 매핑: " + category);
            }
        });
        categoryToAlias = Collections.unmodifiableMap(c2a);

        // Tag -> Category (각 Tag는 정확히 하나의 Category)
        EnumMap<Tag, Category> t2c = new EnumMap<>(Tag.class);
        categoryToTags.forEach((category, tags) -> {
            for (Tag tag : tags) {
                Category prev = t2c.put(tag, category);
                if (prev != null && prev != category) {
                    throw new IllegalStateException(
                            "Tag가 둘 이상의 Category에 매핑됨: " + tag + " (" + prev + " vs " + category + ")"
                    );
                }
            }
        });
        tagToCategory = Collections.unmodifiableMap(t2c);
    }

    // ----------- Public API -----------

    /** Alias -> Category */
    public static Category categoryFrom(Alias alias) {
        return aliasToCategory.get(alias);
    }

    /** Category -> Tags */
    public static List<Tag> tagsFrom(Category category) {
        return categoryToTags.getOrDefault(category, List.of());
    }

    /** Category -> Alias */
    public static Alias aliasFrom(Category category) {
        return categoryToAlias.get(category);
    }

    /** Tag -> Category */
    public static Category categoryFrom(Tag tag) {
        return tagToCategory.get(tag);
    }
}
