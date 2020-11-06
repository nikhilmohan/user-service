package com.nikhilm.hourglass.userservice.repositories;

import com.nikhilm.hourglass.userservice.models.UserSession;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface UserRepository extends ReactiveMongoRepository<UserSession, String> {
}
