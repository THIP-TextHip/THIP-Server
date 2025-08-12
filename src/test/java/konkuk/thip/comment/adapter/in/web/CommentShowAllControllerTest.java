package konkuk.thip.comment.adapter.in.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static konkuk.thip.common.exception.code.ErrorCode.POST_TYPE_NOT_MATCH;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("[단위] 댓글 조회 api controller 단위 테스트")
class CommentShowAllControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("댓글 조회 api 요청 시 request param의 PostType 값이 유효하지 않는 경우, 400 에러가 발생한다.")
    void comment_show_all_post_type_invalid() throws Exception {
        //given
        String invalidPostType = "invalidPostType";

        //when //then
        mockMvc.perform(get("/comments/{postId}", 1L)
                        .requestAttr("userId", 1L)
                        .param("postType", invalidPostType))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(POST_TYPE_NOT_MATCH.getCode()))
                .andExpect(jsonPath("$.message", containsString(POST_TYPE_NOT_MATCH.getMessage())));
    }
}
