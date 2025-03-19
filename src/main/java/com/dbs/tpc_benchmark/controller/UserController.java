package com.dbs.tpc_benchmark.controller;

import com.dbs.tpc_benchmark.config.Result;
import com.dbs.tpc_benchmark.config.JWTutil;
import com.dbs.tpc_benchmark.typings.dto.UserRegisterDTO;
import com.dbs.tpc_benchmark.typings.entity.User;
import com.dbs.tpc_benchmark.service.UserService;
import com.dbs.tpc_benchmark.repository.UserRepository;
import com.dbs.tpc_benchmark.typings.dto.UserLoginDTO;
import com.dbs.tpc_benchmark.typings.vo.StatusVO;
import com.dbs.tpc_benchmark.typings.vo.UserApprovalVO;
import com.dbs.tpc_benchmark.typings.vo.UserDeleteVO;
import com.dbs.tpc_benchmark.typings.vo.UserInfoVO;
import com.dbs.tpc_benchmark.typings.vo.UserListVO;

import org.springframework.beans.factory.annotation.Autowired;
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
    public Result<UserInfoVO> loginUser(@RequestBody UserLoginDTO user) {
        System.out.println("Login API called for user: " + user.getName());
        Map<String, Object> res = userService.loginUser(user);
        boolean success = (boolean) res.get("success");
        String message = (String) res.get("message");

        if (success) {
            User existingUser = userRepository.findByName(user.getName());
            String token = jwtutil.generateToken(existingUser);

            UserInfoVO userLoginVO = UserInfoVO.builder()
                    .role(existingUser.getRole())
                    .name(existingUser.getName())
                    .authorization(token)
                    .build();
            return Result.success(userLoginVO, message);
        }
        return Result.error(message);
    }

    // Admin APIs

    @GetMapping("/all")
    public Result<UserListVO> getAllUsers(HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role))
            return Result.forbidden("Access denied");

        List<User> users = userRepository.findAll();
        List<Map<String, String>> userInfoList = new ArrayList<>();
        
        for (User user : users) {
            Map<String, String> userInfo = new HashMap<>();
            userInfo.put("name", user.getName());
            userInfo.put("status", user.getStatus());
            userInfo.put("role", user.getRole());
            userInfoList.add(userInfo);
        }
        
        UserListVO userListVO = UserListVO.builder()
            .users(userInfoList)
            .build();
        
        return Result.success(userListVO, "Get all users");
    }

    @GetMapping("/pending")
    public Result<UserListVO> getPendingUsers(HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role))
            return Result.forbidden("Access denied");

        List<User> pendingUsers = userRepository.findByStatus("PENDING");
        List<Map<String, String>> userInfoList = new ArrayList<>();
        
        for (User user : pendingUsers) {
            Map<String, String> userInfo = new HashMap<>();
            userInfo.put("name", user.getName());
            userInfo.put("status", user.getStatus());
            userInfoList.add(userInfo);
        }
        
        UserListVO userListVO = UserListVO.builder()
            .users(userInfoList)
            .build();
        
        return Result.success(userListVO, "Get pending users");
    }

    @PostMapping("/approve")
    public Result<UserApprovalVO> approveUser(HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role))
            return Result.forbidden("Access denied");

        Map<String, Object> res = userService.approveUser();
        boolean success = (boolean) res.get("success");
        String message = (String) res.get("message");

        if (success) {
            UserApprovalVO vo = UserApprovalVO.builder()
                    .count((int) res.get("count"))
                    .usernames((List<String>) res.get("approvedUsers"))
                    .build();
            System.out.println("Successfully approved " + res.get("count") + " users");
            return Result.success(vo, message);
        } 
        else {
            System.out.println("Failed to approve users: " + message);
            return Result.error(message);
        }
    }

    @DeleteMapping("/delete")
    public Result<UserDeleteVO> deleteUser(@RequestParam String username, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role))
            return Result.forbidden("Access denied");

        Map<String, Object> res = userService.deleteUser(username);
        boolean success = (boolean) res.get("success");
        String message = (String) res.get("message");
        if (success) {
            UserDeleteVO deleteVO = UserDeleteVO.builder()
                    .name(username)
                    .build();
            return Result.success(deleteVO, message);
        } 
        else
            return Result.error(message);
    }
}
