package com.dbs.tpc_benchmark.service;

import com.dbs.tpc_benchmark.mapper.TableMapper;
import com.dbs.tpc_benchmark.typings.dto.TableCreateDTO;
import com.dbs.tpc_benchmark.typings.dto.ColumnDTO;
import com.dbs.tpc_benchmark.typings.constant.ColumnType;
import com.dbs.tpc_benchmark.typings.vo.ProgressVO;
import com.dbs.tpc_benchmark.typings.vo.TableVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class TableService {
    @Autowired
    private TableMapper tableMapper;
    @Autowired
    private ProgressStorageService progressStorageService;

    @Transactional
    public Map<String, Object> createTable(TableCreateDTO tableCreateDTO) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 验证表名
            String tableName = tableCreateDTO.getTableName();
            if (tableName == null || tableName.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "tableName cannot be empty");
                return result;
            }
            
            // 检查表是否已存在
            if (tableMapper.checkTableExists(tableName) != null) {
                result.put("success", false);
                result.put("message", "Table '" + tableName + "' already exists");
                return result;
            }
            
            // 验证列信息
            List<ColumnDTO> columns = tableCreateDTO.getColumns();
            if (columns == null || columns.isEmpty()) {
                result.put("success", false);
                result.put("message", "columns cannot be empty");
                return result;
            }
            
            // 构建SQL
            StringBuilder createTableSQL = new StringBuilder();
            createTableSQL.append("CREATE TABLE ").append(tableName).append(" (");
            
            // 添加列定义
            List<String> primaryKeys = new ArrayList<>();
            for (int i = 0; i < columns.size(); i++) {
                ColumnDTO column = columns.get(i);
                
                if (column.getName() == null || column.getName().trim().isEmpty()) {
                    result.put("success", false);
                    result.put("message", "column name cannot be empty");
                    return result;
                }
                
                // 添加列名和类型
                createTableSQL.append(column.getName()).append(" ");
                createTableSQL.append(getColumnTypeDefinition(column.getType(), column.getLength()));
                
                // 添加约束
                if (column.isNotNull()) {
                    createTableSQL.append(" NOT NULL");
                }
                
                if (column.getLowerLimit() != null || column.getUpperLimit() != null) {
                    createTableSQL.append(" CHECK (");
                    
                    if (column.getLowerLimit() != null) {
                        createTableSQL.append(column.getName()).append(" >= ").append(column.getLowerLimit());
                        
                        if (column.getUpperLimit() != null) {
                            createTableSQL.append(" AND ");
                        }
                    }
                    
                    if (column.getUpperLimit() != null) {
                        createTableSQL.append(column.getName()).append(" <= ").append(column.getUpperLimit());
                    }
                    
                    createTableSQL.append(")");
                }

                if (column.isPrimaryKey()) {
                    primaryKeys.add(column.getName());
                }
                
                if (i < columns.size() - 1 || !primaryKeys.isEmpty()) {
                    createTableSQL.append(", ");
                }
            }
            
            // 添加主键
            if (!primaryKeys.isEmpty()) {
                createTableSQL.append("PRIMARY KEY (");
                for (int i = 0; i < primaryKeys.size(); i++) {
                    createTableSQL.append(primaryKeys.get(i));
                    if (i < primaryKeys.size() - 1) {
                        createTableSQL.append(", ");
                    }
                }
                createTableSQL.append(")");
            }
            
            createTableSQL.append(");");
            
            System.out.println("Executing SQL: " + createTableSQL);
            
            // 执行SQL
            tableMapper.executeDDL(createTableSQL.toString());
            
            // 设置成功响应
            result.put("success", true);
            result.put("message", "Table created successfully");
            result.put("tableName", tableName);
            result.put("columns", columns);
            
            // 获取表信息
            Map<String, Object> tableInfo = tableMapper.getTableInfo(tableName);
            if (tableInfo != null) {
                result.put("lastupdate", tableInfo.get("update_time"));
                result.put("size", tableInfo.get("data_length"));
            }
            
            return result;
            
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "Error creating table: " + e.getMessage());
            return result;
        }
    }

    private String getColumnTypeDefinition(ColumnType type, Integer length) {
        if (type == null)
            return "VARCHAR(255)";
        
        switch (type) {
            case INTEGER:
                return "INT" + (length != null ? "(" + length + ")" : "");
            case VARCHAR:
                return "VARCHAR" + (length != null ? "(" + length + ")" : "(255)");
            case CHAR:
                return "CHAR" + (length != null ? "(" + length + ")" : "(1)");
            case DECIMAL:
                return "DECIMAL" + (length != null ? "(" + length + ",2)" : "(10,2)");
            case DATE:
                return "DATE";
            default:
                return "VARCHAR(255)";
        }
    }

    public List<TableVO> getAllTables() {
        List<String> tableNames = tableMapper.getAllTables();
        List<TableVO> tables = new ArrayList<>();
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        for (String tableName : tableNames) {
            Map<String, Object> tableInfo = tableMapper.getTableInfo(tableName);
            ProgressVO progressVO = progressStorageService.findProcessingProgressByTableName(tableName);
            Date updateTime = null;
            if (tableInfo.get("update_time") instanceof Date) {
                updateTime = (Date) tableInfo.get("update_time");
            }
            
            String lastUpdate = updateTime != null ? dateFormat.format(updateTime) : "unknown";
            
            TableVO vo = TableVO.builder()
                    .tablename(tableName)
                    .lastupdate(lastUpdate)
                    .build();
            if (progressVO != null) {
                vo.setProgressVO(progressVO);
            }
            tables.add(vo);
        }
        
        return tables;
    }

    public TableVO getTableByName(String tableName) {
        if (tableMapper.checkTableExists(tableName) == null) {
            return null;
        }
        
        Map<String, Object> tableInfo = tableMapper.getTableInfo(tableName);
        if (tableInfo == null) {
            return null;
        }
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        Date updateTime = null;
        if (tableInfo.get("update_time") instanceof Date) {
            updateTime = (Date) tableInfo.get("update_time");
        }
        
        String lastUpdate = updateTime != null ? dateFormat.format(updateTime) : "unknown";
        
        return TableVO.builder()
                .tablename(tableName)
                .lastupdate(lastUpdate)
                .build();
    }
}