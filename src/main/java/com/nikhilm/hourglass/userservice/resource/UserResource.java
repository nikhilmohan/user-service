package com.nikhilm.hourglass.userservice.resource;

import com.nikhilm.hourglass.userservice.models.UserCred;
import com.nikhilm.hourglass.userservice.models.UserDTO;
import com.nikhilm.hourglass.userservice.models.UserSession;
import com.nikhilm.hourglass.userservice.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@RestController
@Configuration
@PropertySource("classpath:.env")
@Slf4j
public class UserResource {

    @Value("${apikey}")
    private String apiKey;

    @Value("${idp.url}")
    private String idpUrl;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserMapper userMapper;

    @PostMapping("/signup")
    public Mono<ResponseEntity<UserDTO>> signUpUser(@RequestBody UserCred credentials)    {

        WebClient client = WebClient.create(idpUrl + ":signUp");
        return client.post().uri("?key="+apiKey)
                .bodyValue(credentials)
                .retrieve()
                .bodyToMono(UserSession.class)
                .flatMap(userSession -> {
                    log.info("ID: "  + userSession.getLocalId());
                    WebClient favClient = WebClient.create("http://localhost:9900/favourites-service/favourites/");
                    return favClient.post().uri("user/" + userSession.getLocalId())
                            .exchange()
                            .flatMap(clientResponse -> {
                                userSession.setCreatedTime(LocalDateTime.now());
                                return userRepository.save(userSession);
                            });
                })
                .map(userMapper::userSessionToResponse)
                .map(userDTO -> ResponseEntity.ok().body(userDTO));
    }
    @PostMapping("/login")
    public Mono<ResponseEntity<UserDTO>> login(@RequestBody UserCred credentials)    {

        WebClient client = WebClient.create(idpUrl + ":signInWithPassword");
        return client.post().uri("?key="+apiKey)
                .bodyValue(credentials)
                .retrieve()
                .bodyToMono(UserSession.class)
                .flatMap(userSession -> {
                    userSession.setCreatedTime(LocalDateTime.now());
                    return userRepository.save(userSession);
                })
                .map(userMapper::userSessionToResponse)
                .map(userDTO -> ResponseEntity.ok().body(userDTO));
    }
    @GetMapping("/user/{userId}/status")
    public Mono<Boolean> getUserStatus(@PathVariable("userId") String userId)   {
        return userRepository.findById(userId)
                .filter(session -> session.getCreatedTime().plusSeconds(Long.parseLong(session.getExpiresIn()))
                        .isAfter(LocalDateTime.now()))
                .flatMap(userSession -> Mono.just(true))
                .switchIfEmpty(Mono.defer(()->Mono.just(false)));
    }
    @PostMapping("/logout")
    public Mono<ResponseEntity<Void>> logout(@RequestBody UserDTO userDTO)  {
        return userRepository.findById(userDTO.getLocalId())
                .switchIfEmpty(Mono.defer(()->Mono.error(new RuntimeException("No session found!"))))
                .flatMap(userRepository::delete)
                .then(Mono.just(ResponseEntity.noContent().build()));
    }
}
