package com.hrmanproject.domain.auth.enums;

public enum EmployeeTypes {
    NULL,
    ENTRY,
    JUNIOR,
    SENIOR,
    MANAGER,
    BOARD_MEMBER;

    public boolean canAssignProjectTo(EmployeeTypes target) {
        if (target == null || target == NULL) return false;
        return switch (this) {
            case BOARD_MEMBER -> target == MANAGER || target == SENIOR || target == JUNIOR || target == ENTRY;
            case MANAGER -> target == SENIOR || target == JUNIOR || target == ENTRY;
            case SENIOR -> target == JUNIOR || target == ENTRY;
            default -> false;
        };
    }

    public boolean isManagerial() {
        return this == MANAGER || this == BOARD_MEMBER;
    }
}
