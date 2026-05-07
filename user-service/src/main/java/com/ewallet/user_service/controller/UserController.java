package com.ewallet.user_service.controller;

import com.ewallet.user_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/")
    public ResponseEntity<?> home(){
        return ResponseEntity.ok("Welcome to User Service!");
    }


    @GetMapping("/profile")
    public ResponseEntity<?> getUser(@RequestHeader("X-User-Id") Long id){
        return userService.getUser(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.ok("There is no user"));
    }

    @DeleteMapping("/profile")
    public ResponseEntity<?> deleteUser(@RequestHeader("X-User-Id") Long id){
        return userService.getUser(id)
                .map(user -> {
                    userService.deleteUser(id);
                    return ResponseEntity.ok("User deleted successfully");
                })
                .orElse(ResponseEntity.ok("There is no user to delete"));
    }

}
