package com.vodovod.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "organizations")
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Naziv organizacije je obavezan")
    @Size(max = 200, message = "Naziv može imati maksimalno 200 znakova")
    @Column(name = "name")
    private String name;

    @NotBlank(message = "Oznaka (slug) je obavezna")
    @Size(max = 50, message = "Oznaka može imati maksimalno 50 znakova")
    @Column(name = "slug", unique = true)
    private String slug;

    @Column(name = "enabled")
    private boolean enabled = true;

    public Organization() {}

    public Organization(String name, String slug) {
        this.name = name;
        this.slug = slug;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}


