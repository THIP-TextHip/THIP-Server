package konkuk.thip.book.adapter.in.web;

import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.book.adapter.out.persistence.repository.SavedBookJpaRepository;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.user.adapter.out.persistence.repository.alias.AliasJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
@DisplayName("[통합] 저장한 책 조회 API 통합 테스트")
class BookShowSavedListApiTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private UserJpaRepository userJpaRepository;
    @Autowired private AliasJpaRepository aliasJpaRepository;
    @Autowired private BookJpaRepository bookJpaRepository;
    @Autowired private SavedBookJpaRepository savedBookJpaRepository;

    private UserJpaEntity user;
    private BookJpaEntity savedBook;

    @BeforeEach
    void setUp() {
        AliasJpaEntity alias = aliasJpaRepository.save(TestEntityFactory.createLiteratureAlias());
        user = userJpaRepository.save(TestEntityFactory.createUser(alias));
        savedBook = bookJpaRepository.save(TestEntityFactory.createBookWithISBN("1111111111111"));
        savedBookJpaRepository.save(TestEntityFactory.createSavedBook(user, savedBook));
    }

    @Test
    @DisplayName("사용자가 저장한 책을 정상적으로 호출한다.")
    void getSavedBooks_success() throws Exception {
        mockMvc.perform(get("/books/saved")
                        .requestAttr("userId", user.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bookList").isArray())
                .andExpect(jsonPath("$.data.bookList.length()").value(1))
                .andExpect(jsonPath("$.data.bookList[0].isbn").value(savedBook.getIsbn()));
    }

}