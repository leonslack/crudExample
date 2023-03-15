package com.clever.example.dto.user;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class UserRequest {
    private long id;

    private String name;

    private String lastName;

    private String email;

    private String cellphone;

    private Date birthdate;
}
