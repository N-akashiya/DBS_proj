package com.dbs.tpc_benchmark.service;

import com.dbs.tpc_benchmark.model.User;
import com.dbs.tpc_benchmark.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public String registerUser(User user) {
        if (userRepository.findByName(user.getName()) != null) {
            return "User already exists";
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("USER");
        user.setStatus("PENDING");
        userRepository.save(user);
        return "Please wait for approval";
    }

    public String loginUser(User user) {
        User existingUser = userRepository.findByName(user.getName());
        if (existingUser == null) {
            return "User not found";
        }
        System.out.println("Found user: " + existingUser.getName() + ", status: " + existingUser.getStatus());
        if ("PENDING".equals(existingUser.getStatus())) {
            return "User not approved";
        }
        boolean passwordMatches = false;
        try {
            passwordMatches = passwordEncoder.matches(user.getPassword(), existingUser.getPassword());
            System.out.println("Matching: " + passwordMatches);
        } catch (Exception e) {
            System.out.println("Matching error: " + e.getMessage());
            e.printStackTrace();
            return "Error matching password";
        }
        if (!passwordMatches) {
            return "Invalid password";
        }
        return "Login successful:)";
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getPendingUsers() {
        return userRepository.findByStatus("PENDING");
    }

    // 目前全部pending的用户都会被approve
    public String approveUser(String username) {
        User pendingUser = userRepository.findByName(username);
        if (pendingUser != null) {
            pendingUser.setStatus("APPROVED");
            userRepository.save(pendingUser);
            return "User approved";
        }
        return "User not found";
    }

    public String deleteUser(String username) {
        User user = userRepository.findByName(username);
        if (user != null) {
            userRepository.delete(user);
            return "User deleted";
        }
        return "User not found";
    }
}
