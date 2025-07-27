package konkuk.thip.common.swagger;

import konkuk.thip.common.exception.code.ErrorCode;
import lombok.Getter;

import java.util.LinkedHashSet;
import java.util.Set;

import static konkuk.thip.common.exception.code.ErrorCode.*;

@Getter
public enum SwaggerResponseDescription {
//
//    //Auth
    LOGIN(new LinkedHashSet<>(Set.of(
            USER_NOT_FOUND
    ))),
    LOGOUT(new LinkedHashSet<>(Set.of(

    ))),

//    //User
    USER_SIGNUP(new LinkedHashSet<>(Set.of(
    ))),
    USER_CHOICE_ALIAS(new LinkedHashSet<>(Set.of(
    ))),

    // Follow
    GET_USER_FOLLOW(new LinkedHashSet<>(Set.of(
            USER_NOT_FOUND
    ))),


  ;
    private final Set<ErrorCode> errorCodeList;
    SwaggerResponseDescription(Set<ErrorCode> errorCodeList) {
        // 공통 에러
        errorCodeList.addAll(new LinkedHashSet<>(Set.of(
                API_NOT_FOUND,
                API_METHOD_NOT_ALLOWED,
                API_SERVER_ERROR,

//                API_BAD_REQUEST,
//                API_MISSING_PARAM,
//                API_INVALID_PARAM,
//                API_INVALID_TYPE

                AUTH_INVALID_TOKEN,
                AUTH_EXPIRED_TOKEN,
                AUTH_UNAUTHORIZED,
                AUTH_TOKEN_NOT_FOUND
//                AUTH_LOGIN_FAILED,
//                AUTH_UNSUPPORTED_SOCIAL_LOGIN,

//                JSON_PROCESSING_ERROR
        )));


        this.errorCodeList = errorCodeList;
    }
}
