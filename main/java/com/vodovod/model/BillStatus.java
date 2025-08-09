package com.vodovod.model;

public enum BillStatus {
    PENDING("Na čekanju"),
    PAID("Plaćen"),
    PARTIALLY_PAID("Djelomično plaćen"),
    OVERDUE("Dospješan"),
    CANCELLED("Storniran");

    private final String displayName;

    BillStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}