package com.nikhilm.hourglass.userservice.services;

import com.nikhilm.hourglass.userservice.exceptions.UserException;
import com.nikhilm.hourglass.userservice.models.Event;
import com.nikhilm.hourglass.userservice.models.UserCred;
import com.nikhilm.hourglass.userservice.models.UserSession;
import com.nikhilm.hourglass.userservice.repositories.UserRepository;
import com.nikhilm.hourglass.userservice.resource.UserResource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.function.Function;

@Service
@Slf4j
@EnableBinding(UserService.MessageSources.class)
public class UserService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    WebClient webClient;

    @Autowired
    MessageSources messageSources;

    @Autowired
    ReactiveCircuitBreakerFactory factory;

    ReactiveCircuitBreaker rcb;



    public Mono<UserSession> initializeFavouritesSync(UserSession userSession) {
        log.info("invoked initializeFavourites " + userSession.getLocalId());
        return webClient
                .post().uri("http://localhost:9900/favourites-service/favourites")
                .header("Authorization", "Bearer " + userSession.getIdToken())
                .exchange()
                .flatMap(clientResponse -> {
                    if (!clientResponse.statusCode().is2xxSuccessful()) {
                        log.info("Exception thrown " + clientResponse.statusCode());
                        return fallbackUserSession(userSession);
                    }
                    log.info("received response initializeFavourites " + userSession.getLocalId());

                    return Mono.just(userSession);
                });

    }
    public Mono<UserSession> activateUserSession(String uri, UserCred credentials)    {
        return webClient.post().uri(uri)
                .bodyValue(credentials)
                .exchange()
                .flatMap(clientResponse -> {
                    if (clientResponse.statusCode().is2xxSuccessful()) {
                        return clientResponse.bodyToMono(UserSession.class);
                    }
                    log.error("error " + clientResponse.statusCode());
                    if (clientResponse.statusCode().is4xxClientError()) {
                        return Mono.error(new UserException(clientResponse.statusCode().value(), "Invalid credentials!"));
                    }

                    return Mono.error(new UserException(clientResponse.statusCode().value(), "Identity Provider error!"));


                });
    }
//    public Mono<UserSession> loginUser(String uri, UserCred credentials)  {
//        return webClient.post().uri(uri)
//                .bodyValue(credentials)
//                .exchange()
//                .flatMap(clientResponse -> {
//                    if (clientResponse.statusCode().is2xxSuccessful()) {
//                        return clientResponse.bodyToMono(UserSession.class);
//                    }
//                    log.error("error " + clientResponse.statusCode());
//                    if (clientResponse.statusCode().is4xxClientError()) {
//                        return Mono.error(new UserException(clientResponse.statusCode().value(), "Invalid credentials!"));
//                    }
//
//                    return Mono.error(new UserException(clientResponse.statusCode().value(), "Identity Provider error!"));
//
//                });
//    }
    public  Mono<UserSession> initializeDashboardSync(UserSession session) {
        log.info("invoked initializeDashboard " + session.getLocalId());
        return webClient
                .post()
                .uri("http://localhost:9900/dashboard-service/metrics/" + session.getLocalId())
                .header("Authorization", "Bearer " + session.getIdToken())
                .exchange()
                .flatMap(response -> {
                    if (response.statusCode().is5xxServerError()) {

                        return fallbackUserSessionForDashboard(session);
                    }
                    log.info("received response initializeDashboard " + session.getLocalId());
                    return Mono.just(session);
                });
    }
    public void publishUserEvent(UserSession userSession)  {
        UserSession session = new UserSession();
        session.setCreatedTime(userSession.getCreatedTime());
        session.setEmail(userSession.getEmail());
        session.setExpiresIn(userSession.getExpiresIn());
        session.setIdToken(userSession.getIdToken());
        session.setLocalId(userSession.getLocalId());
        session.setRefreshToken(userSession.getRefreshToken());
        messageSources.outputNewusers().send(MessageBuilder
                .withPayload(new Event<String, Object>(Event.Type.USER_PENDING, userSession.getLocalId(),
                        Optional.of(session))).build());
    }
    private Mono<UserSession> fallbackUserSession(UserSession userSession)    {
        log.info("Inside fallback..");
        messageSources.outputUsers().send((MessageBuilder
                .withPayload(new Event(Event.Type.USER_ADDED, userSession.getLocalId(), Optional.empty())).build()));
        log.info("favourites notify event published! " + userSession.getLocalId());
        return Mono.just(userSession);
    }
    public Mono<UserSession> initializeFavourites(UserSession userSession) {
        return initializeFavouritesSync(userSession);
    }
    private Mono<UserSession> fallbackUserSessionForDashboard(UserSession userSession)    {
        log.info("Inside dashboardfallback..");
        messageSources.outputDashboard().send((MessageBuilder
                .withPayload(new Event(Event.Type.USER_ADDED, userSession.getLocalId(), Optional.empty())).build()));
        log.info("dashboard notify event published! " + userSession.getLocalId());
        return Mono.just(userSession);
    }

    public Mono<UserSession> initializeDashboard(UserSession userSession) {
        return initializeDashboardSync(userSession);

    }
    public Mono<UserSession> syncUserRecord(UserSession session) {
        return userRepository.save(session)
                .onErrorMap(throwable -> new UserException(500, "Internal server error!"));

    }
    public interface MessageSources {

        String OUTPUT_USERS = "output-users";
        String OUTPUT_DASHBOARD = "output-dashboard";
        String OUTPUT_NEWUSERS = "output-newusers";


        @Output(OUTPUT_USERS)
        MessageChannel outputUsers();

        @Output(OUTPUT_DASHBOARD)
        MessageChannel outputDashboard();

        @Output(OUTPUT_NEWUSERS)
        MessageChannel outputNewusers();

    }


}
