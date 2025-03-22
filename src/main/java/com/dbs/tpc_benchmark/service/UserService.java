package com.dbs.tpc_benchmark.service;

import com.dbs.tpc_benchmark.typings.entity.User;
import com.dbs.tpc_benchmark.repository.UserRepository;
import com.dbs.tpc_benchmark.typings.dto.UserRegLogDTO;

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

    public Map<String, Object> registerUser(UserRegLogDTO userRegisterDTO) {
        Map<String, Object> res = new HashMap<>();
        User user = new User();
        if (userRepository.findByName(userRegisterDTO.getName()) != null) {
            res.put("success", false);
            res.put("message","User already exists");
            return res;
        }
        user.setName(userRegisterDTO.getName());
        user.setPassword(passwordEncoder.encode(userRegisterDTO.getPassword()));
        user.setRole("USER");
        user.setStatus("PENDING");
        userRepository.save(user);
        res.put("success", true);
        res.put("message", "Please wait for approval");
        return res;
    }

    public Map<String, Object> loginUser(UserRegLogDTO user) {
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

   public Map<String, Object> approveUser(List<Long> userIds) {
       Map<String, Object> res = new HashMap<>();
       
       if (userIds == null || userIds.isEmpty()) {
           res.put("success", false);
           res.put("message", "No users selected");
           return res;
       }

       List<String> approvedUsernames = new ArrayList<>();
       int success_cnt = 0;
       for (Long userId : userIds) {
            Optional<User> optionalUser = userRepository.findById(userId);
            if (optionalUser.isPresent()) {
                User user = optionalUser.get();
                if ("PENDING".equals(user.getStatus())) {
                    user.setStatus("APPROVED");
                    userRepository.save(user);
                    approvedUsernames.add(user.getName());
                    success_cnt++;
                }
            }
       }

       if (success_cnt == 0) {
           res.put("success", false);
           res.put("message", "No users approved");
           return res;
        
       }

       res.put("success", true);
       res.put("message", success_cnt + " users approved");
       res.put("count", success_cnt);
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
