package com.nikhilm.hourglass.userservice.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Document(collection = "users")
public class UserSession {
    private String idToken;
    private String email;
    private String refreshToken;
    private String expiresIn;
    @Id
    private String localId;
    private LocalDateTime createdTime;
}
