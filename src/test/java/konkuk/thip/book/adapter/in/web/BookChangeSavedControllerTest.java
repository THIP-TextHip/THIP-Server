package konkuk.thip.book.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import konkuk.thip.book.adapter.in.web.request.PostBookIsSavedRequest;
import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.common.exception.code.ErrorCode;
import konkuk.thip.common.security.util.JwtUtil;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.saved.adapter.out.jpa.SavedBookJpaEntity;
import konkuk.thip.saved.adapter.out.persistence.repository.SavedBookJpaRepository;
import konkuk.thip.user.adapter.out.jpa.AliasJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserRole;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.user.adapter.out.persistence.repository.alias.AliasJpaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("[통합] BookChangeSavedController 테스트")
class BookChangeSavedControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private AliasJpaRepository aliasJpaRepository;

    @Autowired
    private BookJpaRepository bookJpaRepository;

    @Autowired
    private SavedBookJpaRepository savedBookJpaRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private String testToken;
    private Long userId;
    private String testIsbn = "1234567890123";

    @BeforeEach
    void setUp() {

        AliasJpaEntity alias = aliasJpaRepository.save(TestEntityFactory.createLiteratureAlias());

        UserJpaEntity user = userJpaRepository.save(UserJpaEntity.builder()
                .oauth2Id("kakao_432708231")
                .nickname("User1")
                .nicknameUpdatedAt(LocalDate.now().minusMonths(7))
                .role(UserRole.USER)
                .aliasForUserJpaEntity(alias)
                .build());

        // 테스트책 미리 저장
        BookJpaEntity book = bookJpaRepository.save(BookJpaEntity.builder()
                .isbn(testIsbn)
                .title("테스트책")
                .imageUrl("https://image.url")
                .authorName("저자")
                .publisher("출판사")
                .description("설명")
                .bestSeller(false)
                .build());

        userId = user.getUserId();
        testToken = jwtUtil.createAccessToken(userId);

    }

    @AfterEach
    void tearDown() {
        savedBookJpaRepository.deleteAll();
        bookJpaRepository.deleteAll();
        userJpaRepository.deleteAll();
        aliasJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("DB에 책이 존재하고 해당 책을 저장하려고 할때 [책 저장 성공]")
    void saveBook_success() throws Exception {

        // given
        PostBookIsSavedRequest request = new PostBookIsSavedRequest(true);

        //when
        ResultActions result = mockMvc.perform(post("/books/{isbn}/saved", testIsbn)
                        .header("Authorization", "Bearer " + testToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)));

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isbn").value(testIsbn))
                .andExpect(jsonPath("$.data.isSaved").value(true));

        // 실제 저장됐는지 검증
        Optional<BookJpaEntity> bookJpaEntity = bookJpaRepository.findByIsbn(testIsbn);
        boolean exists = savedBookJpaRepository.existsByUserJpaEntity_UserIdAndBookJpaEntity_BookId(userId, bookJpaEntity.get().getBookId());
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("DB에 책이 존재하지 않을 때 해당 책을 DB에 저장하고, 해당 책을 저장하려고 할 때 [책 저장 성공]")
    void saveBook_whenBookNotExist_thenSaveAndSuccess() throws Exception {

        // given
        String newIsbn = "9791195710447"; // DB에 없고 실제 존재하는 책 ISBN
        PostBookIsSavedRequest request = new PostBookIsSavedRequest(true);

        // when
        ResultActions result = mockMvc.perform(post("/books/{isbn}/saved", newIsbn)
                .header("Authorization", "Bearer " + testToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isbn").value(newIsbn))
                .andExpect(jsonPath("$.data.isSaved").value(true));

        // 실제 저장됐는지 검증
        Optional<BookJpaEntity> bookJpaEntity = bookJpaRepository.findByIsbn(newIsbn);
        assertThat(bookJpaEntity).isPresent();
        boolean exists = savedBookJpaRepository.existsByUserJpaEntity_UserIdAndBookJpaEntity_BookId(userId, bookJpaEntity.get().getBookId());
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("이미 저장된 책을 저장하려고하면 [400 에러 발생]")
    void saveBook_alreadySaved_fail() throws Exception {

        // given
        BookJpaEntity book = bookJpaRepository.findByIsbn(testIsbn).get();
        UserJpaEntity user = userJpaRepository.findById(userId).get();
        savedBookJpaRepository.save(SavedBookJpaEntity.builder()
                .userJpaEntity(user)
                .bookJpaEntity(book)
                .build());

        PostBookIsSavedRequest request = new PostBookIsSavedRequest(true);

        //when
        ResultActions result = mockMvc.perform(post("/books/{isbn}/saved", testIsbn)
                .header("Authorization", "Bearer " + testToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        //then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.BOOK_ALREADY_SAVED.getCode()));

    }

    @Test
    @DisplayName("이미 저장한 책 [책 저장 삭제 성공]")
    void deleteBook_success() throws Exception {

        // given
        BookJpaEntity book = bookJpaRepository.findByIsbn(testIsbn).get();
        UserJpaEntity user = userJpaRepository.findById(userId).get();
        savedBookJpaRepository.save(SavedBookJpaEntity.builder()
                .userJpaEntity(user)
                .bookJpaEntity(book)
                .build());

        PostBookIsSavedRequest request = new PostBookIsSavedRequest(false);

        //when
        ResultActions result = mockMvc.perform(post("/books/{isbn}/saved", testIsbn)
                .header("Authorization", "Bearer " + testToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isbn").value(testIsbn))
                .andExpect(jsonPath("$.data.isSaved").value(false));

        // 실제 삭제됐는지 검증
        boolean exists = savedBookJpaRepository.existsByUserJpaEntity_UserIdAndBookJpaEntity_BookId(user.getUserId(), book.getBookId());
        assertThat(exists).isFalse();

    }

    @Test
    @DisplayName("저장하지 않은 책을 삭제하려고 하면 [400 애러 발생]")
    void deleteBook_notSaved_fail() throws Exception {

        // given
        PostBookIsSavedRequest request = new PostBookIsSavedRequest(false);

        //when
        ResultActions result = mockMvc.perform(post("/books/{isbn}/saved", testIsbn)
                .header("Authorization", "Bearer " + testToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
        //then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.BOOK_NOT_SAVED_CANNOT_DELETE.getCode()));

    }

    @Test
    @DisplayName("DB에 존재하지 않는 책을 삭제하려고 하면 [400 에러 발생]")
    void deleteBook_whenBookNotExist_thenFail() throws Exception {
        // given
        String newIsbn = "9791195710447"; // DB에 없고 실제 존재하는 책 ISBN
        PostBookIsSavedRequest request = new PostBookIsSavedRequest(false);

        // when
        ResultActions result = mockMvc.perform(post("/books/{isbn}/saved", newIsbn)
                .header("Authorization", "Bearer " + testToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.BOOK_NOT_SAVED_DB_CANNOT_DELETE.getCode()));
    }

}
