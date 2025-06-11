package com.dbs.tpc_benchmark.service;

import com.dbs.tpc_benchmark.repository.LogRepository;
import com.dbs.tpc_benchmark.typings.entity.Log;
import com.dbs.tpc_benchmark.typings.dto.ImportDTO;
import com.dbs.tpc_benchmark.typings.vo.ProgressVO;
import com.opencsv.CSVReader;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReaderBuilder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ImportService {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private LogRepository logRepository;

    @Autowired
    private ProgressStorageService progressService;

    @Value("${import.log.path:logs}")
    private String logBasePath;

    private static final int BATCH_SIZE = 50;

    private static final int UPLOAD_INTERVAL = 500;

    @Async("importTaskExecutor")
    public void asyncImportData(ImportDTO importDTO, String taskId) {
        Map<String, Object> res = importData(importDTO, taskId);
        try {
            ProgressVO vo = progressService.getProgress(taskId);
            boolean success = (Boolean) res.get("success");
            vo.setStatus(success ? "COMPLETED" : "FAILED");
        } catch (Exception e) {
            progressService.getProgress(taskId).setStatus("FAILED");
        }
    }

    @Transactional
    public Map<String, Object> importData(ImportDTO importDTO, String taskId) {
        Map<String, Object> res = new HashMap<>();

        String tableName = importDTO.getTableName();
        MultipartFile file = importDTO.getFile();

        if(file == null || file.isEmpty()) {
            res.put("success", false);
            res.put("message", "File path cannot be empty");
            return res;
        }

        File logDir = new File(logBasePath);
        if (!logDir.exists()) {
            logDir.mkdirs();
        }
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String LogFileName = tableName + "_import_" + timestamp + ".log";
        String LogPath = new File(logDir, LogFileName).getAbsolutePath();
        
        long startTime = System.currentTimeMillis();
        try (BufferedWriter logWriter = new BufferedWriter(new FileWriter(LogPath))) {
            logWriter.write("Import Log - " + LocalDateTime.now() + "\n");
            logWriter.write("Table: " + tableName + "\n");
            logWriter.write("File: " + file.getOriginalFilename() + "\n");
            logWriter.write("-------------------------------------\n");

            Map<String, Object> import_res = new HashMap<>();

            switch (tableName) {
            case "ORDERS":
                import_res = importOrdersData(file, startTime, logWriter, LogPath, taskId);
                break;
            case "LINEITEM":
                import_res = importLineItemData(file, startTime, logWriter, LogPath, taskId);
                break;
            default:
                logWriter.write("Unsupported table name: " + tableName + "\n");
                res.put("success", false);
                res.put("message", "Unsupported table name");
                return res;
            }
            logWriter.write("-------------------------------------\n");
            logWriter.write("Import completed in " + (System.currentTimeMillis() - startTime) + " ms\n");
            logWriter.write("Total records: " + import_res.get("totalRecords") + "\n");
            logWriter.write("Imported records: " + import_res.get("importedRecords") + "\n");
            return import_res;
        }
        catch (Exception e) {
            e.printStackTrace();
            res.put("success", false);
            res.put("message", "Error importing data: " + e.getMessage());
            return res;
        }
    }

    private Map<String, Object> importOrdersData(MultipartFile file, long startTime, BufferedWriter logWriter, String logPath, String taskId) throws Exception {
        Map<String, Object> res = new HashMap<>();

        int total = 0; // 读到的行数
        int imported = 0; // 成功导入的行数
        long processedBytes = 0;
        long totalBytes = file.getSize();
        List<Integer> errorLines = new ArrayList<>();
        List<Object[]> batchParams = new ArrayList<>();
        List<Integer> validLines = new ArrayList<>();
        
        try {
            com.opencsv.CSVParser csvParser = new CSVParserBuilder()
                .withSeparator('|')
                .build();
            
            try (CSVReader reader = new CSVReaderBuilder(new InputStreamReader(file.getInputStream()))
                .withCSVParser(csvParser)
                .build()) {
                String[] line;
                int lineNumber = 0;
                
                String sql = "INSERT INTO ORDERS (O_ORDERKEY, O_CUSTKEY, O_ORDERSTATUS, " +
                        "O_TOTALPRICE, O_ORDERDATE, O_ORDERPRIORITY, O_CLERK, O_SHIPPRIORITY, O_COMMENT) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                
                while ((line = reader.readNext()) != null) {
                    lineNumber++;
                    total++;

                    if (total % UPLOAD_INTERVAL == 0) {
                        progressService.updateProgress(taskId, total, imported, totalBytes, processedBytes);
                    }
                    String row = String.join("|", line);
                    processedBytes += row.getBytes(StandardCharsets.UTF_8).length
                            + System.lineSeparator().getBytes(StandardCharsets.UTF_8).length;

                    String originalRow = String.join("|", line);
                    // 检查必要字段是否完整
                    if (line.length < 9) {
                        errorLines.add(lineNumber);
                        logWriter.write(lineNumber + ",\"" + originalRow + "\", the number of fields is less than 9\n");
                        continue;
                    }
                    try {
                        long orderKey = Long.parseLong(line[0]);
                        // 订单金额必须为正数
                        double totalPrice = Double.parseDouble(line[3]);
                        
                        if (orderKey <= 0) {
                            errorLines.add(lineNumber);
                            logWriter.write(lineNumber + ",\"" + originalRow + "\", order key must be a positive integer\n");
                            continue;
                        }

                        if (totalPrice <= 0) {
                            errorLines.add(lineNumber);
                            logWriter.write(lineNumber + ",\"" + originalRow + "\", total price must be a positive number\n");
                            continue;
                        }

                        // 所有验证通过
                        Object[] params = new Object[9];
                        params[0] = orderKey;
                        params[1] = Long.parseLong(line[1]);  // O_CUSTKEY
                        params[2] = line[2];                  // O_ORDERSTATUS
                        params[3] = totalPrice;               // O_TOTALPRICE
                        params[4] = line[4];                  // O_ORDERDATE
                        params[5] = line[5];                  // O_ORDERPRIORITY
                        params[6] = line[6];                  // O_CLERK
                        params[7] = Integer.parseInt(line[7]);// O_SHIPPRIORITY
                        params[8] = line[8];                  // O_COMMENT
                        
                        batchParams.add(params);
                        validLines.add(lineNumber);
                        
                        if (batchParams.size() >= BATCH_SIZE) {
                            try {
                                int[] updateCnt = jdbcTemplate.batchUpdate(sql, batchParams);
                                imported += countSuccessfulUpdates(updateCnt);
                            } catch (org.springframework.jdbc.UncategorizedSQLException e) {
                                // 检查是否是触发器抛出的特殊错误
                                if (e.getMessage() != null && e.getMessage().contains("ORDER_EXISTS")) {
                                    // 为可能需要更新的记录创建UPDATE语句
                                    for (int i = 0; i < batchParams.size(); i++) {
                                        try {
                                            Object[] rowParams = batchParams.get(i);
                                            jdbcTemplate.update(
                                                "UPDATE ORDERS SET O_CUSTKEY = ?, O_ORDERSTATUS = ?, " +
                                                "O_TOTALPRICE = ?, O_ORDERDATE = ?, O_ORDERPRIORITY = ?, " +
                                                "O_CLERK = ?, O_SHIPPRIORITY = ?, O_COMMENT = ? " +
                                                "WHERE O_ORDERKEY = ?",
                                                rowParams[1], rowParams[2], rowParams[3], rowParams[4], rowParams[5],
                                                rowParams[6], rowParams[7], rowParams[8], rowParams[0]);
                                            imported++;
                                        } catch (Exception updateEx) {
                                            logWriter.write("Failed to update existing record: " + updateEx.getMessage() + "\n");
                                        }
                                    }
                                } else {// 其他SQL错误
                                    logWriter.write("Error in batch insert: " + e.getMessage() + "\n");
                                }
                            }
                            batchParams.clear();
                            validLines.clear();
                        }
                    }
                    catch (NumberFormatException e) {
                        errorLines.add(lineNumber);
                        logWriter.write(lineNumber + ", invalid number format\n");
                    }
                }
                // 处理剩余的批次
                if (!batchParams.isEmpty()) {
                    int[] updateCnt = jdbcTemplate.batchUpdate(sql, batchParams);
                    imported += countSuccessfulUpdates(updateCnt);
                }
            }
        } catch (Exception e) {
            errorLines.add(-1); // 导入失败
            logWriter.write("Error: " + e.getMessage() + "\n");
        }
        
        saveImportLog("ORDERS", total, imported, errorLines, System.currentTimeMillis() - startTime, logPath);
        
        res.put("success", true);
        res.put("message", "Import completed");
        res.put("tableName", "ORDERS");
        res.put("totalRecords", total);
        res.put("importedRecords", imported);
        return res;
    }

    private Map<String, Object> importLineItemData(MultipartFile file, long startTime, BufferedWriter logWriter, String logPath, String taskId) throws Exception {
        Map<String, Object> res = new HashMap<>();

        int total = 0;
        int imported = 0;
        long processedBytes = 0;
        long totalBytes = file.getSize();
        List<Integer> errorLines = new ArrayList<>();
        List<Object[]> batchParams = new ArrayList<>();
        List<Integer> validLines = new ArrayList<>();

        try {
            com.opencsv.CSVParser csvParser = new CSVParserBuilder()
                .withSeparator('|')
                .build();
            
            try (CSVReader reader = new CSVReaderBuilder(new InputStreamReader(file.getInputStream()))
                .withCSVParser(csvParser)
                .build()) {
                String[] line;
                int lineNumber = 0;
                
                String sql = "INSERT INTO LINEITEM (L_ORDERKEY, L_PARTKEY, L_SUPPKEY, L_LINENUMBER, " +
                        "L_QUANTITY, L_EXTENDEDPRICE, L_DISCOUNT, L_TAX, L_RETURNFLAG, " +
                        "L_LINESTATUS, L_SHIPDATE, L_COMMITDATE, L_RECEIPTDATE, L_SHIPINSTRUCT, " +
                        "L_SHIPMODE, L_COMMENT) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                
                while ((line = reader.readNext()) != null) {
                    lineNumber++;
                    total++;

                    if (total % UPLOAD_INTERVAL == 0) {
                        progressService.updateProgress(taskId, total, imported, totalBytes, processedBytes);
                    }
                    String row = String.join("|", line);
                    processedBytes += row.getBytes(StandardCharsets.UTF_8).length
                            + System.lineSeparator().getBytes(StandardCharsets.UTF_8).length;

                    String originalRow = String.join("|", line);
                    if (line.length < 16) {
                        errorLines.add(lineNumber);
                        logWriter.write(lineNumber + ",\"" + originalRow + "\", the number of fields is less than 16\n");
                        continue;
                    }
                    try {
                        // 主键检查
                        long orderKey = Long.parseLong(line[0]);
                        long partKey = Long.parseLong(line[1]);
                        long suppKey = Long.parseLong(line[2]);
                        int lineNum = Integer.parseInt(line[3]);
                        // 数量必须为正，折扣必须在0-1之间
                        double quantity = Double.parseDouble(line[4]);
                        double extendedPrice = Double.parseDouble(line[5]);
                        double discount = Double.parseDouble(line[6]);
                        double tax = Double.parseDouble(line[7]);
                        
                        if (orderKey <= 0) {
                            errorLines.add(lineNumber);
                            logWriter.write(lineNumber + ",\"" + originalRow + "\", order key must be a positive integer\n");
                            continue;
                        }
                        
                        if (lineNum <= 0) {
                            errorLines.add(lineNumber);
                            logWriter.write(lineNumber + ",\"" + originalRow + "\", line number must be a positive integer\n");
                            continue;
                        }
                        
                        if (quantity <= 0) {
                            errorLines.add(lineNumber);
                            logWriter.write(lineNumber + ",\"" + originalRow + "\", quantity must be a positive number\n");
                            continue;
                        }
                        
                        if (discount < 0 || discount > 1) {
                            errorLines.add(lineNumber);
                            logWriter.write(lineNumber + ",\"" + originalRow + "\", discount must be between 0 and 1\n");
                            continue;
                        }
                        
                        // 所有验证通过
                        Object[] params = new Object[16];
                        params[0] = orderKey;                  // L_ORDERKEY
                        params[1] = partKey;                   // L_PARTKEY
                        params[2] = suppKey;                   // L_SUPPKEY
                        params[3] = lineNum;                   // L_LINENUMBER
                        params[4] = quantity;                  // L_QUANTITY
                        params[5] = extendedPrice;             // L_EXTENDEDPRICE
                        params[6] = discount;                  // L_DISCOUNT
                        params[7] = tax;                       // L_TAX
                        params[8] = line[8];                   // L_RETURNFLAG
                        params[9] = line[9];                   // L_LINESTATUS
                        params[10] = line[10];                 // L_SHIPDATE
                        params[11] = line[11];                 // L_COMMITDATE
                        params[12] = line[12];                 // L_RECEIPTDATE
                        params[13] = line[13];                 // L_SHIPINSTRUCT
                        params[14] = line[14];                 // L_SHIPMODE
                        params[15] = line[15];                 // L_COMMENT
                        
                        batchParams.add(params);
                        validLines.add(lineNumber);
                        
                        if (batchParams.size() >= BATCH_SIZE) {
                            try {
                                int[] updateCnt = jdbcTemplate.batchUpdate(sql, batchParams);
                                imported += countSuccessfulUpdates(updateCnt);
                            } catch (org.springframework.jdbc.UncategorizedSQLException e) {
                                if (e.getMessage() != null && e.getMessage().contains("LINEITEM_EXISTS")) {
                                    for (int i = 0; i < batchParams.size(); i++) {
                                        try {
                                            Object[] rowParams = batchParams.get(i);
                                            jdbcTemplate.update(
                                                "UPDATE LINEITEM SET L_PARTKEY = ?, L_SUPPKEY = ?, " +
                                                "L_QUANTITY = ?, L_EXTENDEDPRICE = ?, L_DISCOUNT = ?, " +
                                                "L_TAX = ?, L_RETURNFLAG = ?, L_LINESTATUS = ?, " +
                                                "L_SHIPDATE = ?, L_COMMITDATE = ?, L_RECEIPTDATE = ?, " +
                                                "L_SHIPINSTRUCT = ?, L_SHIPMODE = ?, L_COMMENT = ? " +
                                                "WHERE L_ORDERKEY = ? AND L_LINENUMBER = ?",
                                                rowParams[1], rowParams[2], rowParams[4], rowParams[5], 
                                                rowParams[6], rowParams[7], rowParams[8], rowParams[9], 
                                                rowParams[10], rowParams[11], rowParams[12], rowParams[13], 
                                                rowParams[14], rowParams[15], rowParams[0], rowParams[3]);
                                            imported++;
                                        } catch (Exception updateEx) {
                                            logWriter.write("Failed to update existing record: " + updateEx.getMessage() + "\n");
                                        }
                                    }
                                } else {
                                    logWriter.write("Error in batch insert: " + e.getMessage() + "\n");
                                }
                            }
                            batchParams.clear();
                            validLines.clear();
                        }
                    } catch (NumberFormatException e) {
                        errorLines.add(lineNumber);
                        logWriter.write(lineNumber + ", invalid number format\n");
                    }
                }
                // 处理剩余的批次
                if (!batchParams.isEmpty()) {
                    int[] updateCnt = jdbcTemplate.batchUpdate(sql, batchParams);
                    imported += countSuccessfulUpdates(updateCnt);
                }
            }
        } catch (Exception e) {
            errorLines.add(-1);
            logWriter.write("Error: " + e.getMessage() + "\n");
        }
        
        saveImportLog("LINEITEM", total, imported, errorLines, System.currentTimeMillis() - startTime, logPath);
        
        res.put("success", true);
        res.put("message", "Import completed");
        res.put("tableName", "LINEITEM");
        res.put("totalRecords", total);
        res.put("importedRecords", imported);
        return res;
    }

    private void saveImportLog(String tableName, int totalRows, int successRows, List<Integer> errorLines, long elapsedTimeMs, String logPath) {
        try {
            String errorLinesStr = "";
            if (!errorLines.isEmpty()) {
                errorLinesStr = String.join(",", 
                    errorLines.stream().map(String::valueOf).toArray(String[]::new));
            }
    
            Log log = Log.builder()
                .tableName(tableName)
                .importTime(LocalDateTime.now())
                .totalRows(totalRows)
                .successRows(successRows)
                .failedRows(errorLines.size())
                .errorLines(errorLinesStr)
                .build();
    
            logRepository.save(log);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int countSuccessfulUpdates(int[] updateCnt) {
        int cnt = 0;
        for (int i : updateCnt) {
            if (i > 0) // 更新成功
                cnt++;
        }
        return cnt;
    }
}
