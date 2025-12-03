package com.zjsu.gjh.catalog.model;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Embeddable
public class Instructor {

    @NotBlank(message = "讲师编号不能为空")
    private String id;

    @NotBlank(message = "讲师姓名不能为空")
    private String name;

    @Email(message = "讲师邮箱格式不正确")
    @NotBlank(message = "讲师邮箱不能为空")
    private String email;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}


