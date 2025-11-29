package com.app.fairfree.enums;

public enum ItemStatus {
    PRIVATE, // just owned by the user, not visible to the user.
    AVAILABLE, // others can see this as available and claim it.
    ON_HOLD, // someone claimed it as I am interested.
    DONATED, // after approval.
    EXPIRED
}
