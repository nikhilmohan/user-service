package com.nikhilm.hourglass.userservice.resource;

import com.nikhilm.hourglass.userservice.exceptions.ApiError;
import com.nikhilm.hourglass.userservice.exceptions.UserException;
import com.nikhilm.hourglass.userservice.models.UserCred;
import com.nikhilm.hourglass.userservice.models.UserDTO;
import com.nikhilm.hourglass.userservice.models.UserSession;
import com.nikhilm.hourglass.userservice.repositories.UserRepository;
import com.nikhilm.hourglass.userservice.services.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@WebFluxTest
class UserResourceTest {

    @MockBean
    UserService userService;

    @MockBean
    UserRepository userRepository;

    @Autowired
    WebTestClient webTestClient;

    @MockBean
    UserMapper userMapper;

  


    @Test
    public void testGetUserStatus() {

        UserSession userSession = new UserSession();
        userSession.setLocalId("abc");
        userSession.setRefreshToken("rtoken");
        userSession.setExpiresIn("3600");
        userSession.setEmail("test@abc.com");
        userSession.setCreatedTime(LocalDateTime.now());
        Mockito.when(userRepository.findById("abc")).thenReturn(Mono.just(userSession));
        Boolean status = webTestClient.get().uri("http://localhost:9070/user/abc/status")
                .exchange()
                .expectBody(Boolean.class)
                .returnResult()
                .getResponseBody();

        assertEquals(true, status);

    }

    @Test
    public void testGetUserStatusNotLoggedIn()  {
        Mockito.when(userRepository.findById("abc")).thenReturn(Mono.empty());
        Boolean status = webTestClient.get().uri("http://localhost:9070/user/abc/status")
                .exchange()
                .expectBody(Boolean.class)
                .returnResult()
                .getResponseBody();

        assertEquals(false, status);
    }
    @Test
    public void testLogoutInvalid()  {
        UserDTO userDTO = new UserDTO();
        userDTO.setLocalId("abc");
        userDTO.setRefreshToken("rtoken");
        userDTO.setExpiresIn("3600");
        userDTO.setEmail("test@abc.com");

        Mockito.when(userRepository.findById("abc")).thenReturn(Mono.empty());
        webTestClient.post().uri("http://localhost:9070/logout")
                .header("user", "abcd")
                .body(Mono.just(userDTO), UserDTO.class)
                .exchange()
                .expectStatus()
                .is5xxServerError();


    }
    @Test
    public void testSignup()    {

        UserCred userCred = new UserCred();
        userCred.setEmail("test.abc.com");
        userCred.setPassword("abcdef");
        userCred.setReturnSecureToken(true);

        UserSession userSession = new UserSession();
        userSession.setLocalId("abc");
        userSession.setRefreshToken("rtoken");
        userSession.setExpiresIn("3600");
        userSession.setEmail("test@abc.com");
        userSession.setIdToken("somejwttoken");

        UserDTO userDTO = new UserDTO();
        userDTO.setLocalId("abc");
        userDTO.setRefreshToken("rtoken");
        userDTO.setExpiresIn("3600");
        userDTO.setEmail("test@abc.com");
        userDTO.setIdToken("somejwttoken");
        doReturn(Mono.just(userSession)).when(userService).activateUserSession(anyString(), any(UserCred.class));
        when(userRepository.save(userSession)).thenReturn(Mono.just(userSession));
        when(userService.initializeFavourites(userSession)).thenReturn(Mono.just(userSession));
        when(userService.initializeDashboard(userSession)).thenReturn(Mono.just(userSession));
        when(userMapper.userSessionToResponse(userSession)).thenReturn(userDTO);
        UserDTO response = webTestClient.post().uri("http://localhost:9070/signup")
                .body(Mono.just(userCred), UserCred.class)
                .exchange()
                .expectBody(UserDTO.class)
                .returnResult()
                .getResponseBody();

        assertEquals("abc", response.getLocalId());
        assertEquals("somejwttoken", response.getIdToken());

    }
    @Test
    public void testSignupFallback()    {

        UserCred userCred = new UserCred();
        userCred.setEmail("test.abc.com");
        userCred.setPassword("abcdef");
        userCred.setReturnSecureToken(true);

        UserSession userSession = new UserSession();
        userSession.setLocalId("abc");
        userSession.setRefreshToken("rtoken");
        userSession.setExpiresIn("3600");
        userSession.setEmail("test@abc.com");
        userSession.setIdToken("somejwttoken");

        UserDTO userDTO = new UserDTO();
        userDTO.setLocalId("abc");
        userDTO.setRefreshToken("rtoken");
        userDTO.setExpiresIn("3600");
        userDTO.setEmail("test@abc.com");
        userDTO.setIdToken("somejwttoken");
        doReturn(Mono.just(userSession)).when(userService).activateUserSession(anyString(), any(UserCred.class));
        when(userRepository.save(userSession)).thenReturn(Mono.error(new RuntimeException()));
        when(userService.initializeFavourites(userSession)).thenReturn(Mono.just(userSession));
        when(userService.initializeDashboard(userSession)).thenReturn(Mono.just(userSession));
        doNothing().when(userService).publishUserEvent(userSession);
        when(userMapper.userSessionToResponse(userSession)).thenReturn(userDTO);
        UserDTO response = webTestClient.post().uri("http://localhost:9070/signup")
                .body(Mono.just(userCred), UserCred.class)
                .exchange()
                .expectBody(UserDTO.class)
                .returnResult()
                .getResponseBody();

        assertEquals("abc", response.getLocalId());
        assertEquals("somejwttoken", response.getIdToken());

    }


    @Test
    public void testLogin()    {
        UserCred userCred = new UserCred();
        userCred.setEmail("test.abc.com");
        userCred.setPassword("abcdef");
        userCred.setReturnSecureToken(true);

        UserSession userSession = new UserSession();
        userSession.setLocalId("abc");
        userSession.setRefreshToken("rtoken");
        userSession.setExpiresIn("3600");
        userSession.setEmail("test@abc.com");
        userSession.setIdToken("somejwttoken");

        UserDTO userDTO = new UserDTO();
        userDTO.setLocalId("abc");
        userDTO.setRefreshToken("rtoken");
        userDTO.setExpiresIn("3600");
        userDTO.setEmail("test@abc.com");
        userDTO.setIdToken("somejwttoken");
        doReturn(Mono.just(userSession)).when(userService).activateUserSession(anyString(), any(UserCred.class));
        when(userRepository.save(userSession)).thenReturn(Mono.just(userSession));
        when(userMapper.userSessionToResponse(userSession)).thenReturn(userDTO);
        UserDTO response = webTestClient.post().uri("http://localhost:9070/login")
                .body(Mono.just(userCred), UserCred.class)
                .exchange()
                .expectBody(UserDTO.class)
                .returnResult()
                .getResponseBody();

        assertEquals("abc", response.getLocalId());
        assertEquals("somejwttoken", response.getIdToken());


    }
    @Test
    public void testLoginServerError()  {
            UserCred userCred = new UserCred();
            userCred.setEmail("test.abc.com");
            userCred.setPassword("abcdef");
            userCred.setReturnSecureToken(true);

            UserSession userSession = new UserSession();
            userSession.setLocalId("abc");
            userSession.setRefreshToken("rtoken");
            userSession.setExpiresIn("3600");
            userSession.setEmail("test@abc.com");
            userSession.setIdToken("somejwttoken");

            UserDTO userDTO = new UserDTO();
            userDTO.setLocalId("abc");
            userDTO.setRefreshToken("rtoken");
            userDTO.setExpiresIn("3600");
            userDTO.setEmail("test@abc.com");
            userDTO.setIdToken("somejwttoken");
            doReturn(Mono.just(userSession)).when(userService).activateUserSession(anyString(), any(UserCred.class));
            when(userRepository.save(userSession)).thenThrow(new RuntimeException());
            when(userMapper.userSessionToResponse(userSession)).thenReturn(userDTO);
            webTestClient.post().uri("http://localhost:9070/login")
                    .body(Mono.just(userCred), UserCred.class)
                    .exchange()
                    .expectStatus()
                    .is5xxServerError();



        }
        @Test
        public void testUserCredEquals()    {
            UserCred cred = new UserCred();
            cred.setReturnSecureToken(true);
            cred.setPassword("abcdef123");
            cred.setEmail("test@abc.com");

            UserCred anotherCred = new UserCred();
            anotherCred.setEmail("test@abc.com");

            assertTrue(cred.equals(anotherCred));
        }


}