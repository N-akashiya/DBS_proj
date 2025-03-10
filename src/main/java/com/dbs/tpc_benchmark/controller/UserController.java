package com.dbs.tpc_benchmark.controller;

import com.dbs.tpc_benchmark.model.User;
import com.dbs.tpc_benchmark.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public String registerUser(@RequestBody User user) {
        return userService.registerUser(user);
    }

    @PostMapping("/login")
    public String loginUser(@RequestBody User user) {
        return userService.loginUser(user);
    }

    @GetMapping("/pending")
    public List<User> getPendingUsers() {
        return userService.getPendingUsers();
    }

    @PostMapping("/approve")
    public String approveUser(@RequestParam String username) {
        return userService.approveUser(username);
    }
}
