package com.zjgsu.gjh.catalog.model;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Embeddable
public class Instructor {
    @NotBlank(message = "Instructor ID is required")
    private String instructorId;

    @NotBlank(message = "Instructor name is required")
    private String instructorName;

    @NotBlank(message = "Instructor email is required")
    @Email(message = "Email should be valid")
    private String instructorEmail;

    public Instructor() {
        // JPA requires no-arg constructor
    }

    public Instructor(String id, String name, String email) {
        this.instructorId = id;
        this.instructorName = name;
        this.instructorEmail = email;
    }

    public String getId() {
        return instructorId;
    }

    public void setId(String id) {
        this.instructorId = id;
    }

    public String getName() {
        return instructorName;
    }

    public void setName(String name) {
        this.instructorName = name;
    }

    public String getEmail() {
        return instructorEmail;
    }

    public void setEmail(String email) {
        this.instructorEmail = email;
    }
}
