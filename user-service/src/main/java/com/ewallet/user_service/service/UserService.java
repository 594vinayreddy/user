package com.ewallet.user_service.service;

import com.ewallet.user_service.config.RabbitMQConfig;
import com.ewallet.user_service.entity.UserProfile;
import com.ewallet.user_service.event.UserPublishEvent;
import com.ewallet.user_service.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserProfileRepository userProfileRepository;
    private final RabbitTemplate rabbitTemplate;

    public Optional<UserProfile> getUser(Long id) {
        return userProfileRepository.findById(id);
    }

    public void deleteUser(Long id) {
        if (userProfileRepository.existsById(id)) {
            UserProfile userProfile = userProfileRepository.findById(id).orElseThrow();
            UserPublishEvent userEvent = new UserPublishEvent();
            userEvent.setUserId(id);
            userEvent.setEmail(userProfile.getEmail());
            userProfileRepository.deleteById(id);
            log.info("User with id {} deleted successfully", id);
            // Notify wallet service to delete the wallet
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE,
                                          RabbitMQConfig.USER_TO_WALLET_ROUTING_KEY,
                                          userEvent);
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE,
                                          RabbitMQConfig.USER_TO_AUTH_ROUTING_KEY,
                                          userEvent);
        } else {
            log.warn("Attempted to delete non-existent user with id {}", id);
        }
    }
}
