package com.nikhilm.hourglass.userservice.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserCred {
    private String email;
    private String password;
    private boolean returnSecureToken = true;
}
