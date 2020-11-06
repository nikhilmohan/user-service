package com.nikhilm.hourglass.userservice.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserDTO {
    private String idToken;
    private String email;
    private String refreshToken;
    private String expiresIn;
    private String localId;
}
