package com.nikhilm.hourglass.userservice.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nikhilm.hourglass.userservice.models.Event;
import com.nikhilm.hourglass.userservice.models.UserCred;
import com.nikhilm.hourglass.userservice.models.UserSession;
import com.nikhilm.hourglass.userservice.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    WebClient webClient;

    @Mock
    ReactiveCircuitBreakerFactory factory;

    @Mock
    UserService.MessageSources messageSources;


    @InjectMocks
    UserService userService;

    @Test
    public void testInitializeFavourites()  {

        UserSession userSession = new UserSession();
        userSession.setLocalId("abc");
        userSession.setRefreshToken("rtoken");
        userSession.setExpiresIn("3600");
        userSession.setEmail("test@abc.com");
        userSession.setCreatedTime(LocalDateTime.now());

        WebClient.RequestBodyUriSpec requestBodyUriSpec
                = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);
        ClientResponse clientResponse = ClientResponse.create(HttpStatus.OK).build();

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString()))
                .thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.exchange()).thenReturn(Mono.just(clientResponse));

        StepVerifier.create(userService.initializeFavouritesSync(userSession))
                .expectSubscription()
                .expectNext(userSession)
                .verifyComplete();


    }

    @Test
    public void testInitializeFavouritesFallback()  {

        UserSession userSession = new UserSession();
        userSession.setLocalId("abc");
        userSession.setRefreshToken("rtoken");
        userSession.setExpiresIn("3600");
        userSession.setEmail("test@abc.com");
        userSession.setCreatedTime(LocalDateTime.now());

        WebClient.RequestBodyUriSpec requestBodyUriSpec
                = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);
        ClientResponse clientResponse = ClientResponse.create(HttpStatus.INTERNAL_SERVER_ERROR).build();
        MessageChannel channel = mock(MessageChannel.class);
        Mockito.when(messageSources.outputUsers()).thenReturn(channel);
        Mockito.when(channel.send(any(Message.class))).thenReturn(true);

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString()))
                .thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.exchange()).thenReturn(Mono.just(clientResponse));

        StepVerifier.create(userService.initializeFavourites(userSession))
                .expectSubscription()
                .expectNext(userSession)
                .verifyComplete();


    }
    @Test
    public void testInitializeDashboard()  {

        UserSession userSession = new UserSession();
        userSession.setLocalId("abc");
        userSession.setRefreshToken("rtoken");
        userSession.setExpiresIn("3600");
        userSession.setEmail("test@abc.com");
        userSession.setCreatedTime(LocalDateTime.now());

        WebClient.RequestBodyUriSpec requestBodyUriSpec
                = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);
        ClientResponse clientResponse = ClientResponse.create(HttpStatus.OK).build();

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString()))
                .thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.exchange()).thenReturn(Mono.just(clientResponse));

        StepVerifier.create(userService.initializeDashboardSync(userSession))
                .expectSubscription()
                .expectNext(userSession)
                .verifyComplete();



    }
    @Test
    public void testInitializeDashboardFallback()  {

        UserSession userSession = new UserSession();
        userSession.setLocalId("abc");
        userSession.setRefreshToken("rtoken");
        userSession.setExpiresIn("3600");
        userSession.setEmail("test@abc.com");
        userSession.setCreatedTime(LocalDateTime.now());

        WebClient.RequestBodyUriSpec requestBodyUriSpec
                = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);
        ClientResponse clientResponse = ClientResponse.create(HttpStatus.INTERNAL_SERVER_ERROR).build();
        MessageChannel channel = mock(MessageChannel.class);
        Mockito.when(messageSources.outputDashboard()).thenReturn(channel);
        Mockito.when(channel.send(any(Message.class))).thenReturn(true);

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString()))
                .thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.exchange()).thenReturn(Mono.just(clientResponse));

        StepVerifier.create(userService.initializeDashboard(userSession))
                .expectSubscription()
                .expectNext(userSession)
                .verifyComplete();


    }

    @Test
    public void testPublishUserEvent()  {
        UserSession userSession = new UserSession();
        userSession.setLocalId("abc");
        userSession.setRefreshToken("rtoken");
        userSession.setExpiresIn("3600");
        userSession.setEmail("test@abc.com");
        userSession.setCreatedTime(LocalDateTime.now());

        ArgumentCaptor<Message> eventArgumentCaptor = ArgumentCaptor.forClass(Message.class);

        MessageChannel channel = mock(MessageChannel.class);
        Mockito.when(messageSources.outputNewusers()).thenReturn(channel);
        Mockito.when(channel.send(eventArgumentCaptor.capture())).thenReturn(true);

        userService.publishUserEvent(userSession);
        Event <String, Object> event = (Event)eventArgumentCaptor.getValue().getPayload();
        UserSession sessionEvent = (UserSession)event.getData().get();
        assertTrue(event.getKey().equalsIgnoreCase("abc") &&
                sessionEvent.getEmail().equalsIgnoreCase(userSession.getEmail()));


    }

    @Test
    public void testInitializeFavouritesFailed()    {

        UserSession userSession = new UserSession();
        userSession.setLocalId("abc");
        userSession.setRefreshToken("rtoken");
        userSession.setExpiresIn("3600");
        userSession.setEmail("test@abc.com");
        userSession.setCreatedTime(LocalDateTime.now());
        MessageChannel channel = mock(MessageChannel.class);

        WebClient.RequestBodyUriSpec requestBodyUriSpec
                = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);
        ClientResponse clientResponse = ClientResponse.create(HttpStatus.INTERNAL_SERVER_ERROR).build();

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString()))
                .thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.exchange()).thenReturn(Mono.just(clientResponse));
        Mockito.when(messageSources.outputUsers()).thenReturn(channel);
        Mockito.when(channel.send(any(Message.class))).thenReturn(true);

        StepVerifier.create(userService.initializeFavouritesSync(userSession))
                .expectSubscription()
                .expectNext(userSession)
                .verifyComplete();

        verify(messageSources).outputUsers();
    }
    @Test
    public void testInitializeDashboardFailed()    {

        UserSession userSession = new UserSession();
        userSession.setLocalId("abc");
        userSession.setRefreshToken("rtoken");
        userSession.setExpiresIn("3600");
        userSession.setEmail("test@abc.com");
        userSession.setCreatedTime(LocalDateTime.now());

        WebClient.RequestBodyUriSpec requestBodyUriSpec
                = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);
        ClientResponse clientResponse = ClientResponse.create(HttpStatus.INTERNAL_SERVER_ERROR).build();
        MessageChannel channel = mock(MessageChannel.class);

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString()))
                .thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.exchange()).thenReturn(Mono.just(clientResponse));
        Mockito.when(messageSources.outputDashboard()).thenReturn(channel);
        Mockito.when(channel.send(any(Message.class))).thenReturn(true);

        StepVerifier.create(userService.initializeDashboardSync(userSession))
                .expectSubscription()
                .expectNext(userSession)
                .verifyComplete();

        verify(messageSources).outputDashboard();

    }

    @Test
    public void testActivateUser()  {

        UserSession userSession = new UserSession();
        userSession.setLocalId("abc");
        userSession.setRefreshToken("rtoken");
        userSession.setExpiresIn("3600");
        userSession.setEmail("test@abc.com");

        ObjectMapper objectMapper = new ObjectMapper();
        String body = null;
        try {
            body = objectMapper.writeValueAsString(userSession);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        WebClient.RequestBodyUriSpec requestBodyUriSpec
                = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        ClientResponse clientResponse = ClientResponse.create(HttpStatus.OK)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(body).build();

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString()))
                .thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any(UserCred.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.exchange()).thenReturn(Mono.just(clientResponse));

        StepVerifier.create(userService.activateUserSession("/", new UserCred()))
                .expectSubscription()
                .expectNextMatches(userSession1 -> userSession.getLocalId().equalsIgnoreCase(userSession1.getLocalId()))
                .verifyComplete();

    }
    @Test
    public void testActivateUserClientError()  {


        WebClient.RequestBodyUriSpec requestBodyUriSpec
                = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        ClientResponse clientResponse = ClientResponse.create(HttpStatus.BAD_REQUEST)
                .build();

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString()))
                .thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any(UserCred.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.exchange()).thenReturn(Mono.just(clientResponse));

        StepVerifier.create(userService.activateUserSession("/", new UserCred()))
                .expectSubscription()
                .expectErrorMessage("Invalid credentials!")
                .verify();

    }
    @Test
    public void testActivateUserServerError()  {


        WebClient.RequestBodyUriSpec requestBodyUriSpec
                = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        ClientResponse clientResponse = ClientResponse.create(HttpStatus.SERVICE_UNAVAILABLE)
                .build();

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString()))
                .thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any(UserCred.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.exchange()).thenReturn(Mono.just(clientResponse));

        StepVerifier.create(userService.activateUserSession("/", new UserCred()))
                .expectSubscription()
                .expectErrorMessage("Identity Provider error!")
                .verify();

    }

    @Test
    public void testSyncUserRecord() {
        UserSession userSession = new UserSession();
        userSession.setLocalId("abc");
        Mockito.when(userRepository.save(any(UserSession.class))).thenReturn(Mono.just(userSession));
        StepVerifier.create(userService.syncUserRecord(new UserSession()))
                .expectSubscription()
                .expectNextMatches(userSession1 -> userSession1.getLocalId().equalsIgnoreCase("abc"))
                .verifyComplete();
    }

    @Test
    public void testSyncUserRecordFailure() {
        UserSession userSession = new UserSession();
        userSession.setLocalId("abc");
        Mockito.when(userRepository.save(any(UserSession.class))).thenReturn(Mono.error(new RuntimeException()));
        StepVerifier.create(userService.syncUserRecord(new UserSession()))
                .expectSubscription()
                .expectErrorMessage("Internal server error!")
                .verify();
    }


}