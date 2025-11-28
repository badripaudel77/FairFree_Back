package com.app.fairfree.enums;

public enum ItemStatus {
    PRIVATE, // just owned by the user, not visible to the user
    AVAILABLE, // others can see this as available.
    ON_HOLD, // someone claimed it as I am interested.
    DONATED, // For the person who donated (after approved).
    RECEIVED // For the person who received.
}
