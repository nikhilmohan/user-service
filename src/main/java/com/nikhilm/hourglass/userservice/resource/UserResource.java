package com.nikhilm.hourglass.userservice.resource;

import com.nikhilm.hourglass.userservice.exceptions.ApiError;
import com.nikhilm.hourglass.userservice.exceptions.UserException;
import com.nikhilm.hourglass.userservice.models.Event;
import com.nikhilm.hourglass.userservice.models.UserCred;
import com.nikhilm.hourglass.userservice.models.UserDTO;
import com.nikhilm.hourglass.userservice.models.UserSession;
import com.nikhilm.hourglass.userservice.repositories.UserRepository;

import com.nikhilm.hourglass.userservice.services.UserService;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.function.Function;

import static org.springframework.web.reactive.function.BodyInserters.fromValue;

@RestController
@Configuration
@PropertySource(value = "classpath:.env", ignoreResourceNotFound = true)
@Slf4j
@OpenAPIDefinition(
        info = @Info(
                title = "User service API",
                version = "1.0",
                description = "API for user management in hourglass application",
                contact = @Contact(name = "Nikhil Mohan", email = "nikmohan81@gmail.com")
        )
)
public class UserResource {

    @Value("${apikey}")
    private String apiKey;

    @Value("${idp.url}")
    private String idpUrl;


    @Autowired
    UserRepository userRepository;

    @Autowired
    UserMapper userMapper;


    @Autowired
    UserService userService;


    ReactiveCircuitBreakerFactory factory;

    final ReactiveCircuitBreaker rcb;

    public UserResource( ReactiveCircuitBreakerFactory rcbf) {
        this.factory = rcbf;
        this.rcb = this.factory.create("favourites");
    }
    @Operation(summary = "Signup a new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "New user signed up",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDTO.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid credentials",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class)) })})
    @PostMapping("/signup")
    public Mono<ResponseEntity<UserDTO>> signUpUser(@RequestBody UserCred credentials)    {


        String uri = idpUrl + ":signUp?key=" + apiKey;

        return userService.activateUserSession(uri, credentials)
                .flatMap(userSession -> {
                    log.info("ID: "  + userSession.getLocalId());
                    String authHeader = "Bearer " + userSession.getIdToken();
                    userSession.setCreatedTime(LocalDateTime.now());
                    return rcb.run(userRepository.save(userSession),
                            throwable -> {
                                log.warn("Falling back to save user! " + userSession.getLocalId());
                                userService.publishUserEvent(userSession);
                                return Mono.just(userSession);
                            });
                 })
                .flatMap(userService::initializeFavourites)
                .flatMap(userService::initializeDashboard)
                .map(userMapper::userSessionToResponse)
                .map(userDTO -> ResponseEntity.ok().body(userDTO));
    }



    @Operation(summary = "User login")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User login successful",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDTO.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid credentials",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class)) })})
    @PostMapping("/login")
    public Mono<ResponseEntity<UserDTO>> login(@RequestBody UserCred credentials)    {
        log.info("Received request for login " + credentials.getEmail());
        return userService.activateUserSession(idpUrl + ":signInWithPassword?key="+apiKey, credentials)
                .flatMap(userSession -> {
                    userSession.setCreatedTime(LocalDateTime.now());
                    return rcb.run(userRepository.save(userSession),
                            throwable -> Mono.error(new UserException(500, "Internal server error!")));
                })
                .map(userMapper::userSessionToResponse)
                .map(userDTO -> ResponseEntity.ok().body(userDTO));
    }
    @Operation(summary = "Get user login status - true if user is logged in")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Boolean.class)) })})
    @GetMapping("/user/{userId}/status")
    public Mono<Boolean> getUserStatus(@PathVariable("userId") String userId)   {
        return userRepository.findById(userId)
                .filter(session -> session.getCreatedTime().plusSeconds(Long.parseLong(session.getExpiresIn()))
                        .isAfter(LocalDateTime.now()))
                .flatMap(userSession -> Mono.just(true))
                .switchIfEmpty(Mono.defer(()->Mono.just(false)));
    }
    @Operation(summary = "User logout")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User logout successful",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "No session found",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class)) })})
    @PostMapping("/logout")
    public Mono<ResponseEntity<Void>> logout(@RequestBody UserDTO userDTO)  {
        return userRepository.findById(userDTO.getLocalId())
                .switchIfEmpty(Mono.defer(()->Mono.error(new UserException(500, "No session found!"))))
                .flatMap(userRepository::delete)
                .then(Mono.just(ResponseEntity.noContent().build()));
    }



}
