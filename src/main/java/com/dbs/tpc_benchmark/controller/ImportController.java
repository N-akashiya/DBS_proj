package com.dbs.tpc_benchmark.controller;

import com.dbs.tpc_benchmark.config.Result;
import com.dbs.tpc_benchmark.service.ImportService;
import com.dbs.tpc_benchmark.service.ProgressStorageService;
import com.dbs.tpc_benchmark.typings.dto.ImportDTO;
import com.dbs.tpc_benchmark.typings.vo.ImportResultVO;

import com.dbs.tpc_benchmark.typings.vo.ProgressVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;


@RestController
@RequestMapping("/import")
public class ImportController {
    @Autowired
    private ImportService importService;

    @Autowired
    private ProgressStorageService progressStorageService;

    @PostMapping("/data")
    public Result<ImportResultVO> importData(@RequestParam("tableName") String tableName, @RequestParam("file") MultipartFile file, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            return Result.forbidden("Access denied: Only administrators can import data");
        }

        if (tableName == null || tableName.isEmpty()) {
            return Result.error("Table name cannot be empty");
        }

        ImportDTO importDTO = new ImportDTO();
        importDTO.setTableName(tableName);
        importDTO.setFile(file);
        String taskId = progressStorageService.createTask(tableName);

        ImportResultVO vo = ImportResultVO.builder()
                .tableName(tableName)
                .taskId(taskId)
                .build();

        importService.asyncImportData(importDTO, taskId);
        return Result.success(vo, "导入任务已提交");
    }

    @GetMapping("/progress")
    public Result<ProgressVO> getImportProgress(@RequestParam String taskId) {
        ProgressVO vo = progressStorageService.getProgress(taskId);
        if (vo == null) {
            return Result.error("Invalid task ID");
        }
        return Result.success(vo, "查询进度");
    }
}
