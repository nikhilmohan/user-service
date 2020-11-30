package com.nikhilm.hourglass.userservice.models;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserCred {
    private String email;
    private String password;
    private boolean returnSecureToken = true;


    @Override
    public int hashCode() {
        return super.hashCode();
    }
    @Override
    public boolean equals(Object o) {
         if (o == this) return true;

        if (!(o instanceof UserCred))   {
            return false;
        } else  {
            UserCred other = (UserCred)o;
            if(this.getEmail().equalsIgnoreCase(other.getEmail()))  {
                return true;
            }
        }
        return false;

    }
}

