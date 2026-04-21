package com.ewallet.user_service.service;

import com.ewallet.user_service.entity.UserProfile;
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
            userProfileRepository.deleteById(id);
            log.info("User with id {} deleted successfully", id);
            // Notify wallet service to delete the wallet
            rabbitTemplate.convertAndSend("user.wallet.exchange", "user.wallet.delete", id);
        } else {
            log.warn("Attempted to delete non-existent user with id {}", id);
        }
    }
}
