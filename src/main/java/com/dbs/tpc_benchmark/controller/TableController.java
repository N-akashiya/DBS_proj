package com.dbs.tpc_benchmark.controller;

import com.dbs.tpc_benchmark.config.Result;
import com.dbs.tpc_benchmark.service.TableService;
import com.dbs.tpc_benchmark.typings.dto.TableCreateDTO;
import com.dbs.tpc_benchmark.typings.vo.TableVO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.*;

@RestController
@RequestMapping("/table")
public class TableController {

    @Autowired
    private TableService tableService;

    @PostMapping("/create")
    public Result<TableVO> createTable(@RequestBody TableCreateDTO tableCreateDTO, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role))
            return Result.forbidden("Access denied");
        
        Map<String, Object> result = tableService.createTable(tableCreateDTO);
        boolean success = (boolean) result.get("success");
        String message = (String) result.get("message");
        if (success) {
            String tableName = tableCreateDTO.getTableName();
            TableVO vo = tableService.getTableByName(tableName);
            return Result.success(vo, message);
        }
        else
            return Result.error(message);
    }

    @GetMapping("/{tableName}")
    public Result<TableVO> getTableByName(@PathVariable String tableName, HttpServletRequest request) {
        TableVO table = tableService.getTableByName(tableName);
        if (table == null) {
            return Result.error("Table not found");
        }
        return Result.success(table, "Get table successfully");
    }
}
