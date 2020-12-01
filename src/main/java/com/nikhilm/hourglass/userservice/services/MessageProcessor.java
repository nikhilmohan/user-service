package com.nikhilm.hourglass.userservice.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nikhilm.hourglass.userservice.exceptions.UserException;
import com.nikhilm.hourglass.userservice.models.Event;
import com.nikhilm.hourglass.userservice.models.UserSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.MessageChannel;


@EnableBinding(MessageProcessor.MessageSink.class)
@Slf4j
public class MessageProcessor {

    @Autowired
    UserService userService;


    @StreamListener(target = MessageSink.INPUT_NEWUSERS)
    public void processUserEvents(Event<String, Object> event) {

        log.info("Process message created at {}...", event.getEventCreatedAt());

        switch (event.getEventType()) {

            case USER_PENDING:
                log.info("Added user with ID: {}", event.getKey());
                UserSession session = null;
                if (event.getData().isPresent())    {
                    try {
                        ObjectMapper objectMapper = new ObjectMapper();
                        objectMapper.registerModule(new JavaTimeModule());
                        session = objectMapper.convertValue(event.getData().orElse(new UserSession()), UserSession.class);
                        userService.syncUserRecord(session).block();
                    } catch(Exception e)   {
                        log.error("Exception " + e.getMessage());
                        throw new UserException(500, "Data type error!");
                    }


                } else  {
                    throw new UserException(500, "User record parse failed!");
                }
                break;
            default:
                String errorMessage = "Incorrect event type: " + event.getEventType() + ", expected a relevant goal event";
                log.warn(errorMessage);
                throw new RuntimeException(errorMessage);

        }

        log.info("Message processing done!");

    }
    public interface MessageSink {

        String INPUT_NEWUSERS = "input-newusers";

        @Input(INPUT_NEWUSERS)
        MessageChannel inputNewusers();


    }
}
