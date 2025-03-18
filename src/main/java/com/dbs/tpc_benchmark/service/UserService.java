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

    public Map<String, Object> registerUser(User user) {
        Map<String, Object> res = new HashMap<>();
        if (userRepository.findByName(user.getName()) != null) {
            res.put("success", false);
            res.put("message","User already exists");
            return res;
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("USER");
        user.setStatus("PENDING");
        userRepository.save(user);
        res.put("success", true);
        res.put("message", "Please wait for approval");
        return res;
    }

    public Map<String, Object> loginUser(User user) {
        Map<String, Object> res = new HashMap<>();
        User existingUser = userRepository.findByName(user.getName());
        if (existingUser == null) {
            res.put("success", false);
            res.put("message", "User not found");
            return res;
        }
        System.out.println("Found user: " + existingUser.getName() + ", status: " + existingUser.getStatus());
        if ("PENDING".equals(existingUser.getStatus())) {
            res.put("success", false);
            res.put("message", "User not approved");
            return res;
        }
        boolean passwordMatches = false;
        try {
            passwordMatches = passwordEncoder.matches(user.getPassword(), existingUser.getPassword());
            System.out.println("Matching: " + passwordMatches);
        } catch (Exception e) {
            System.out.println("Matching error: " + e.getMessage());
            e.printStackTrace();
            res.put("success", false);
            res.put("message", "Error matching password");
            return res;
        }
        if (!passwordMatches) {
            res.put("success", false);
            res.put("message", "Invalid password");
            return res;
        }
        res.put("success", true);
        res.put("message", "Login successful");
        System.out.println("Login successful:)");
        return res;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getPendingUsers() {
        return userRepository.findByStatus("PENDING");
    }

    // 目前全部pending的用户都会被approve
    public Map<String, Object> approveUser() {
        Map<String, Object> res = new HashMap<>();
        List<User> pendingUsers = userRepository.findByStatus("PENDING");
        
        if (pendingUsers.isEmpty()) {
            res.put("success", false);
            res.put("message", "No pending users");
            return res;
        }
        
        List<String> approvedUsernames = new ArrayList<>();
        for (User user : pendingUsers) {
            user.setStatus("APPROVED");
            userRepository.save(user);
            approvedUsernames.add(user.getName());
        }
        
        res.put("success", true);
        res.put("message", "All pending users approved");
        res.put("count", approvedUsernames.size());
        res.put("approvedUsers", approvedUsernames);
        
        return res;
    }

    public Map<String, Object> deleteUser(String username) {
        Map<String, Object> res = new HashMap<>();
        User user = userRepository.findByName(username);
        if (user == null) {
            res.put("success", false);
            res.put("message", "User not found");
            return res;
        }
        if ("ADMIN".equals(user.getRole())) {
            res.put("success", false);
            res.put("message", "Cannot delete administrators");
            return res;   
        }    
        userRepository.delete(user);
        res.put("success", true);
        res.put("message", "User deleted");
        return res;
    }
}
