package konkuk.thip.common.post;

public interface CountUpdatable { //TODO 패키지 구조 충돌안나게 한번에 옮기기
    void increaseCommentCount();
    void decreaseCommentCount();
    void updateLikeCount(boolean like);
    Long getId();
}