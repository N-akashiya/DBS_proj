package com.dbs.tpc_benchmark.controller;

import com.dbs.tpc_benchmark.config.JWTutil;
import com.dbs.tpc_benchmark.model.User;
import com.dbs.tpc_benchmark.service.UserService;
import com.dbs.tpc_benchmark.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private JWTutil jwtutil;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody User user) {
        String res = userService.registerUser(user);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User user) {
        String res = userService.loginUser(user);
        if (res.equals("Login successful:)")) {
            User existingUser = userRepository.findByName(user.getName());
            String token = jwtutil.generateToken(existingUser);
            return ResponseEntity.ok(token);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllUsers(HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/pending")
    public ResponseEntity<?> getPendingUsers(HttpServletRequest request) {
        return ResponseEntity.ok(userService.getPendingUsers());
    }

    @PostMapping("/approve")
    public ResponseEntity<?> approveUser(@RequestParam String username, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        return ResponseEntity.ok(userService.approveUser(username));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteUser(@RequestParam String username, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        return ResponseEntity.ok(userService.deleteUser(username));
    }
}
