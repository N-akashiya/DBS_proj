package com.dbs.tpc_benchmark.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ExportService {
    @Autowired
    private QueryService queryService;
    
    private static final List<String> EXPORTABLE_TABLES = Arrays.asList(
        // tpc-h
        "ORDERS", "REGION", "NATION", "SUPPLIER", "PART", "PARTSUPP", "CUSTOMER", "LINEITEM",
        // tpc-c
        "C_WAREHOUSE", "C_DISTRICT", "C_CUSTOMER", "C_HISTORY", "C_ORDERS", "C_NEW_ORDER", 
        "C_ORDER_LINE", "C_STOCK", "C_ITEM"
    );
    
    public List<String> getExportableTables() {
        return EXPORTABLE_TABLES;
    }

    public String exportTableToCsv(String tableName, String exportPath) throws Exception {
        if (!EXPORTABLE_TABLES.contains(tableName.toUpperCase())) {
            throw new IllegalArgumentException("Unsupported table: " + tableName);
        }
        if (!verifyExportPath(exportPath)) {
            throw new IllegalArgumentException("Invalid export path: " + exportPath);
        }
        List<Map<String, Object>> tableData = queryService.getData(tableName);

        String fileName = tableName + ".csv";
        String filePath = new File(exportPath, fileName).getAbsolutePath();
        
        // 写入CSV
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            // 表属性名称
            String headerLine = tableData.get(0).keySet().stream()
                .collect(Collectors.joining(","));
            writer.write(headerLine);
            writer.newLine();
            // 数据
            for (Map<String, Object> row : tableData) {
                String dataLine = row.values().stream()
                    .map(this::escapeField)
                    .collect(Collectors.joining(","));
                writer.write(dataLine);
                writer.newLine();
            }
        }
        return filePath;
    }
    
    private String escapeField(Object field) {
        if (field == null)
            return "";
        
        String value = String.valueOf(field);
        
        // 如果字段包含逗号、引号或换行符，需要用引号包围并转义内部引号
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
    
    public boolean verifyExportPath(String path) {
        try {
            File dir = new File(path);
            if (!dir.exists()) {
                return dir.mkdirs();
            }
            return dir.isDirectory() && dir.canWrite();
        } catch (Exception e) {
            return false;
        }
    }
}