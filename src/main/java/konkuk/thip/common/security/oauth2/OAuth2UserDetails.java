package konkuk.thip.common.security.oauth2;

// 구글, 카카오 소셜 로그인 통합을 위한 인터페이스
public interface OAuth2UserDetails {
    String getProvider(); // (e.g., "kakao", "google", etc.)
    String getProviderId();
}
