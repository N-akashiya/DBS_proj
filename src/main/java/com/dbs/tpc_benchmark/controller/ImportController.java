package com.dbs.tpc_benchmark.controller;

import com.dbs.tpc_benchmark.config.Result;
import com.dbs.tpc_benchmark.service.ImportService;
import com.dbs.tpc_benchmark.typings.dto.ImportDTO;
import com.dbs.tpc_benchmark.typings.vo.ImportResultVO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

@RestController
@RequestMapping("/import")
public class ImportController {
    @Autowired
    private ImportService importService;

    @PostMapping("/data")
    public Result<ImportResultVO> importData(@ModelAttribute ImportDTO importDTO, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            return Result.forbidden("Access denied: Only administrators can import data");
        }

        if (importDTO.getTableName() == null || importDTO.getTableName().isEmpty()) {
            return Result.error("Table name cannot be empty");
        }

        Map<String, Object> res = importService.importData(importDTO);
        boolean success = (boolean) res.get("success");
        String message = (String) res.get("message");
        
        if (success) {
            ImportResultVO vo = ImportResultVO.builder()
                    .tableName((String) res.get("tableName"))
                    .totalRecords((int) res.get("totalRecords"))
                    .importedRecords((int) res.get("importedRecords"))
                    .build();
            
            return Result.success(vo, message);
        } else {
            return Result.error(message);
        }
    }
}
