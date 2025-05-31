package com.dbs.tpc_benchmark.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DBManageService {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Map<String, Object>> getActiveConnections() {
        String sql = "SELECT id, user, host, db, command, time, state, info " +
                "FROM information_schema.processlist " +
                "ORDER BY time DESC";
        return jdbcTemplate.queryForList(sql);
    }

    // 变量查询（数据库配置）
    public List<Map<String, Object>> getDatabaseVariables(String pattern) {
        if (pattern != null && !pattern.isEmpty()) {
            return jdbcTemplate.queryForList(
                "SHOW GLOBAL VARIABLES WHERE Variable_name LIKE ?", 
                "%" + pattern + "%"
            );
        } else {
            return jdbcTemplate.queryForList("SHOW GLOBAL VARIABLES");
        }
    }

    // 状态查询（数据库运行情况和性能）
    public List<Map<String, Object>> getDatabaseStatus(String pattern) {
        if (pattern != null && !pattern.isEmpty()) {
            return jdbcTemplate.queryForList(
                "SHOW GLOBAL STATUS WHERE Variable_name LIKE ?", 
                "%" + pattern + "%"
            );
        } else {
            return jdbcTemplate.queryForList("SHOW GLOBAL STATUS");
        }
    }

    public Map<String, Object> getTableDetailInfo(String schemaName, String tableName) {
        Map<String, Object> result = new HashMap<>();
        
        // 表基本信息
        String tableSql = "SELECT * FROM information_schema.TABLES " +
                "WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ?";
        List<Map<String, Object>> tableInfos = jdbcTemplate.queryForList(tableSql, schemaName, tableName);
        
        if (!tableInfos.isEmpty()) {
            result.put("tableInfo", tableInfos.get(0));
            
            // 索引
            String indexSql = "SELECT INDEX_NAME, COLUMN_NAME, NON_UNIQUE, " +
                    "SEQ_IN_INDEX, CARDINALITY " +
                    "FROM information_schema.STATISTICS " +
                    "WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? " +
                    "ORDER BY INDEX_NAME, SEQ_IN_INDEX";
            List<Map<String, Object>> indexInfos = jdbcTemplate.queryForList(indexSql, schemaName, tableName);
            result.put("indexes", indexInfos);
            
            // 磁盘分区
            try {
                String createTableSql = "SHOW CREATE TABLE " + schemaName + "." + tableName;
                Map<String, Object> createTableInfo = jdbcTemplate.queryForMap(createTableSql);
                String createTableStatement = (String) createTableInfo.get("Create Table");
                
                // 提取物理存储位置信息
                Map<String, Object> physicalStorage = new HashMap<>();
                physicalStorage.put("createStatement", createTableStatement);
                
                // 检查DATA DIRECTORY
                if (createTableStatement.contains("DATA DIRECTORY")) {
                    int dataDirectoryIndex = createTableStatement.indexOf("DATA DIRECTORY");
                    if (dataDirectoryIndex > 0) {
                        int startQuote = createTableStatement.indexOf('\'', dataDirectoryIndex);
                        int endQuote = createTableStatement.indexOf('\'', startQuote + 1);
                        if (startQuote > 0 && endQuote > startQuote) {
                            String dataDirectory = createTableStatement.substring(startQuote + 1, endQuote);
                            physicalStorage.put("dataDirectory", dataDirectory);
                        }
                    }
                }
                else {
                    physicalStorage.put("dataDirectory", "Default storage location");
                }
                result.put("physicalStorage", physicalStorage);
            } catch (Exception e) {
                Map<String, Object> errorInfo = new HashMap<>();
                errorInfo.put("error", "Failed to retrieve physical storage information: " + e.getMessage());
                result.put("physicalStorageError", errorInfo);
            }
        }   
        return result; 
    }

    public Map<String, Object> modifyConnectionTimeout(String variableName, String timeoutValue) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 验证参数名
            Set<String> allowedTimeoutParams = new HashSet<>(Arrays.asList(
                "wait_timeout", "interactive_timeout", "connect_timeout"
            ));
            
            if (!allowedTimeoutParams.contains(variableName.toLowerCase())) {
                result.put("success", false);
                result.put("message", "Cannot modify: " + variableName + 
                        ". Allowed parameters are: " + String.join(", ", allowedTimeoutParams));
                return result;
            }
            
            // 验证值的合法性
            try {
                int timeout = Integer.parseInt(timeoutValue);
                
                if (variableName.equalsIgnoreCase("connect_timeout")) {
                    if (timeout < 2 || timeout > 31536000) {
                        result.put("success", false);
                        result.put("message", "connect_timeout should be 2 ~ 31536000 seconds");
                        return result;
                    }
                }
                else {
                    if (timeout < 1 || timeout > 31536000) {
                        result.put("success", false);
                        result.put("message", variableName + " should be 1 ~ 31536000 seconds");
                        return result;
                    }
                }
            } catch (NumberFormatException e) {
                result.put("success", false);
                result.put("message", "Invalid value for " + variableName + ": " + timeoutValue);
                return result;
            }
            
            // 获取修改前的值
            String oldValueSql = "SHOW GLOBAL VARIABLES WHERE Variable_name = ?";
            List<Map<String, Object>> oldValueList = jdbcTemplate.queryForList(oldValueSql, variableName);
            String oldValue = oldValueList.isEmpty() ? "" : (String) oldValueList.get(0).get("Value");
            
            // 修改
            String updateSql = "SET GLOBAL " + variableName + " = " + timeoutValue;
            jdbcTemplate.execute(updateSql);
            
            // 确认修改后的值
            List<Map<String, Object>> newValueList = jdbcTemplate.queryForList(oldValueSql, variableName);
            String newValue = newValueList.isEmpty() ? "" : (String) newValueList.get(0).get("Value");
            
            result.put("success", true);
            result.put("variableName", variableName);
            result.put("oldValue", oldValue);
            result.put("newValue", newValue);
            result.put("message", "Modified " + variableName + " successfully");
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Failed to modify: " + e.getMessage());
        }
        return result;
    }
}
