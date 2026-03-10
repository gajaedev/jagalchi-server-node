package gajeman.jagalchi.jagalchiserver.domain.auth;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserRole {
    ADMIN("ADMIN"),
    USER("USER"),
    GUEST("GUEST");

    private final String value;

    public static UserRole from(String value) {
        if (value == null) {
            return GUEST;
        }
        for (UserRole role : values()) {
            if (role.value.equalsIgnoreCase(value)) {
                return role;
            }
        }
        return GUEST;
    }

    public boolean canEdit() {
        return this == ADMIN || this == USER;
    }
}

