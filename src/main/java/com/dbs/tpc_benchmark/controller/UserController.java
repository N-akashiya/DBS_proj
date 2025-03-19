package com.dbs.tpc_benchmark.controller;

import com.dbs.tpc_benchmark.config.Result;
import com.dbs.tpc_benchmark.config.JWTutil;
import com.dbs.tpc_benchmark.typings.dto.UserRegisterDTO;
import com.dbs.tpc_benchmark.typings.entity.User;
import com.dbs.tpc_benchmark.service.UserService;
import com.dbs.tpc_benchmark.repository.UserRepository;
import com.dbs.tpc_benchmark.typings.constant.LoginStatusConstant;
import com.dbs.tpc_benchmark.typings.dto.UserLoginDTO;
import com.dbs.tpc_benchmark.typings.vo.StatusVO;
import com.dbs.tpc_benchmark.typings.vo.UserLoginVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.*;

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
    public Result<StatusVO> registerUser(@RequestBody UserRegisterDTO user) {
        Map<String, Object> res = userService.registerUser(user);
        boolean success = (boolean) res.get("success");
        String message = (String) res.get("message");

        if (success) {
            StatusVO statusVO = StatusVO.builder()
                    .data(user.getName())
                    .build();
            return Result.success(statusVO, message);
        }
        else
            return Result.error(message);
    }

    @PostMapping("/login")
    public Result<UserLoginVO> loginUser(@RequestBody UserLoginDTO user) {
        System.out.println("Login API called for user: " + user.getName());
        Map<String, Object> res = userService.loginUser(user);
        boolean success = (boolean) res.get("success");
        String message = (String) res.get("message");

        if (success) {
            User existingUser = userRepository.findByName(user.getName());
            String token = jwtutil.generateToken(existingUser);

            UserLoginVO userLoginVO = UserLoginVO.builder()
                    .role(existingUser.getRole())
                    .name(existingUser.getName())
                    .authorization(token)
                    .build();
            return Result.success(userLoginVO, message);
        }
        return Result.error(message);
    }

    // Admin APIs

//    @GetMapping("/all")
//    public ResponseEntity<ApiResponse<List<User>>> getAllUsers(HttpServletRequest request) {
//        String role = (String) request.getAttribute("role");
//        if (!"ADMIN".equals(role))
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Access denied"));
//
//        List<User> users = userRepository.findAll();
//        return ResponseEntity.ok(ApiResponse.success("Get all users", users));
//    }
//
//    @GetMapping("/pending")
//    public ResponseEntity<ApiResponse<List<User>>> getPendingUsers(HttpServletRequest request) {
//        String role = (String) request.getAttribute("role");
//        if (!"ADMIN".equals(role))
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Access denied"));
//
//        List<User> pendingusers = userRepository.findByStatus("PENDING");
//        return ResponseEntity.ok(ApiResponse.success("Get pending users", pendingusers));
//    }
//
//    @PostMapping("/approve")
//    public ResponseEntity<ApiResponse<Object>> approveUser(@RequestParam String username, HttpServletRequest request) {
//        String role = (String) request.getAttribute("role");
//        if (!"ADMIN".equals(role))
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Access denied"));
//
//        Map<String, Object> res = userService.approveUser();
//        boolean success = (boolean) res.get("success");
//        String message = (String) res.get("message");
//
//        if (success) {
//            Map<String, Object> data = new HashMap<>();
//            data.put("count", res.get("count"));
//            data.put("approvedUsers", res.get("approvedUsers"));
//            return ResponseEntity.ok(ApiResponse.success(message, data));
//        }
//        else
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(message));
//    }
//
//    @DeleteMapping("/delete")
//    public ResponseEntity<ApiResponse<Object>> deleteUser(@RequestParam String username, HttpServletRequest request) {
//        String role = (String) request.getAttribute("role");
//        if (!"ADMIN".equals(role))
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Access denied"));
//
//        Map<String, Object> res = userService.deleteUser(username);
//        boolean success = (boolean) res.get("success");
//        String message = (String) res.get("message");
//        if (success) {
//            Map<String, Object> data = new HashMap<>();
//            data.put("name", username);
//            return ResponseEntity.ok(ApiResponse.success(message, data));
//        }
//        else
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(message));
//    }
}
