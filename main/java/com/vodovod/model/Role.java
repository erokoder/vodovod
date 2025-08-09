package com.vodovod.model;

public enum Role {
    ADMIN("Administrator"),
    USER("Korisnik");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}