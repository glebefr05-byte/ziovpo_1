package com.example.rbpo_aeroport.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationUserDto {

    @NotBlank()
    private String name;

    @NotBlank()
    @Email()
    private String email;

    @NotBlank()
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{5,}$")
    private String password;

}
