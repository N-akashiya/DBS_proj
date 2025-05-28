package com.dbs.tpc_benchmark.controller;

import com.dbs.tpc_benchmark.config.Result;
import com.dbs.tpc_benchmark.service.ExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.List;

@RestController
@RequestMapping("/export")
public class ExportController {
    @Autowired
    private ExportService exportService;

    @GetMapping("/tables")
    public Result<List<String>> getExportableTables() {
        try {
            List<String> tables = exportService.getExportableTables();
            return Result.success(tables, "Get exportable tables successfully");
        } catch (Exception e) {
            return Result.error("Fail to get exportable tables: " + e.getMessage());
        }
    }

    @GetMapping("/csv/{tableName}")
    public ResponseEntity<Resource> exportTableToCsv(
            @PathVariable String tableName,
            @RequestParam String exportPath) {
        try {
            String filePath = exportService.exportTableToCsv(tableName, exportPath);
            File file = new File(filePath);
            
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"");
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE);
            
            FileSystemResource resource = new FileSystemResource(file);
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
}