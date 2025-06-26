package konkuk.thip.user.application.port.out;

import konkuk.thip.user.domain.Alias;

public interface AliasCommandPort {

    Alias findById(Long aliasId);
}
