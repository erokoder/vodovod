package com.vodovod.model;

public enum BillStatus {
    PENDING("Nije Plaćen"),
    PAID("Plaćen"),
    PARTIALLY_PAID("Djelomično plaćen"),
    CANCELLED("Storniran");

    private final String displayName;

    BillStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}