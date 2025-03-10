package com.dbs.tpc_benchmark.service;

import com.dbs.tpc_benchmark.model.User;
import com.dbs.tpc_benchmark.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public String registerUser(User user) {
        if (userRepository.findName(user.getName()) != null) {
            return "User already exists";
        }
        user.setRole("USER");
        user.setStatus("PENDING");
        userRepository.save(user);
        return "Please wait for approval";
    }

    public String loginUser(User user) {
        User existingUser = userRepository.findName(user.getName());
        if (existingUser == null) {
            return "User not found";
        }
        if (!existingUser.getPassword().equals(user.getPassword())) {
            return "Invalid password";
        }
        return "Login successful:)";
    }

    public List<User> getPendingUsers() {
        return userRepository.findStatus("PENDING");
    }

    // 目前全部pending的用户都会被approve，这叫审批吗？
    public String approveUser(String username) {
        User pendingUser = userRepository.findName(username);
        if (pendingUser != null) {
            pendingUser.setStatus("APPROVED");
            userRepository.save(pendingUser);
            return "User approved";
        }
        return "User not found";
    }
}
