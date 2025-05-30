package com.dbs.tpc_benchmark.controller;

import com.dbs.tpc_benchmark.service.DBManageService;
import com.dbs.tpc_benchmark.config.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.*;

@RestController
@RequestMapping("/db")
public class DBManageController {
    @Autowired
    private DBManageService dbManageService;

    @Value("${spring.datasource.url}")
    private String dataSourceUrl;

    @GetMapping("/connections")
    public Result<List<Map<String, Object>>> getActiveConnections(HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role))
            return Result.forbidden("Access denied");

        List<Map<String, Object>> connections = dbManageService.getActiveConnections();
        return Result.success(connections, "Get active connections successfully");
    }
    
    @GetMapping("/variables")
    public Result<List<Map<String, Object>>> getDatabaseVariables(
            @RequestParam(required = false) String pattern, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role))
            return Result.forbidden("Access denied");
        
        List<Map<String, Object>> variables = dbManageService.getDatabaseVariables(pattern);
        return Result.success(variables, "Get database variables successfully");
    }

    @GetMapping("/status")
    public Result<List<Map<String, Object>>> getDatabaseStatus(
            @RequestParam(required = false) String pattern, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role))
            return Result.forbidden("Access denied");
        
        List<Map<String, Object>> status = dbManageService.getDatabaseStatus(pattern);
        return Result.success(status, "Get database status successfully");
    }

    @GetMapping("/tables/{tableName}")
    public Result<Map<String, Object>> getTablePhysicalInfo(
            @PathVariable String tableName,
            @RequestParam(required = false, defaultValue = "dbs_proj") String schemaName,
            HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role))
            return Result.forbidden("Access denied");
        
        Map<String, Object> tableInfo = dbManageService.getTableDetailInfo(schemaName, tableName);
        
        if (tableInfo.isEmpty())
            return Result.error(tableName + " does not exist in schema " + schemaName);
        
        return Result.success(tableInfo, "Get table physical info successfully");
    }

    @PostMapping("/connection-timeout/modify")
    public Result<Map<String, Object>> modifyConnectionTimeout(
            @RequestParam String variableName,
            @RequestParam String timeout, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role))
            return Result.forbidden("Access denied");

        Map<String, Object> result = dbManageService.modifyConnectionTimeout(variableName, timeout);
        boolean success = (boolean) result.get("success");
        String message = (String) result.get("message");
        
        if (success) 
            return Result.success(result, message);
        else
            return Result.error(message);
    }
}
