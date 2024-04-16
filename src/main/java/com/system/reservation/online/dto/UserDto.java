package com.system.reservation.online.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private Long id;

    private String studentNumber;

    @NotEmpty
    private String firstName;

    @NotEmpty
    private String middleName;

    @NotEmpty
    private String lastName;

    @Email
    @NotEmpty(message = "Email should not be empty")
    private String email;

    private String contactNumber;

//    @NotEmpty(message = "Password should not be empty")
    private String password;
}
