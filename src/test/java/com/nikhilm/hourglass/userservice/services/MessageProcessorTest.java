package com.nikhilm.hourglass.userservice.services;

import com.nikhilm.hourglass.userservice.exceptions.UserException;
import com.nikhilm.hourglass.userservice.models.Event;
import com.nikhilm.hourglass.userservice.models.UserSession;
import com.nikhilm.hourglass.userservice.services.MessageProcessor;
import com.nikhilm.hourglass.userservice.services.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MessageProcessorTest {

    @Mock
    UserService userService;

    @InjectMocks
    MessageProcessor messageProcessor;

    @Test
    public void testProcessEvent()  {

        UserSession userSession = new UserSession();
        userSession.setLocalId("abc");
        userSession.setRefreshToken("rtoken");
        userSession.setExpiresIn("3600");
        userSession.setEmail("test@abc.com");
        userSession.setIdToken("somejwttoken");

        Event event = new Event<String, Object>(Event.Type.USER_PENDING, userSession.getLocalId(),
                Optional.of(userSession));

        Mockito.when(userService.syncUserRecord(any(UserSession.class))).thenReturn(Mono.just(userSession));
        messageProcessor.processUserEvents(event);
        verify(userService).syncUserRecord(any(UserSession.class));

    }
    @Test
    public void testProcessInvalidEventType()  {

        UserSession userSession = new UserSession();
        userSession.setLocalId("abc");
        userSession.setRefreshToken("rtoken");
        userSession.setExpiresIn("3600");
        userSession.setEmail("test@abc.com");
        userSession.setIdToken("somejwttoken");

        Event event = new Event<String, Object>(Event.Type.USER_ADDED, userSession.getLocalId(),
                Optional.of(userSession));

        assertThrows(RuntimeException.class, ()->messageProcessor.processUserEvents(event));
    }

    @Test
    public void testProcessInvalidEventFormat() {
        Event event = new Event<String, Object>(Event.Type.USER_PENDING, "key",
                Optional.of(new Object()));
        assertThrows(UserException.class, ()->messageProcessor.processUserEvents(event));

    }
    @Test
    public void testProcessEmptyEventPayload() {
        Event event = new Event<String, Object>(Event.Type.USER_PENDING, "key",
                Optional.empty());
        assertThrows(UserException.class, ()->messageProcessor.processUserEvents(event));

    }
}
