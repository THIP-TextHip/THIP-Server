package konkuk.thip.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Embeddable
@Getter
public class UserVoteId implements Serializable {
    private Long userId;
    private Long voteItemId;
}