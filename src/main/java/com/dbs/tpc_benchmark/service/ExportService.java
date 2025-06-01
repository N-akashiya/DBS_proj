package com.dbs.tpc_benchmark.service;

import com.dbs.tpc_benchmark.typings.dto.ClientInfoDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dbs.tpc_benchmark.typings.tableList.ClientInfo;
import com.dbs.tpc_benchmark.typings.tableList.OrderRevenue;
import com.dbs.tpc_benchmark.typings.vo.ClientInfoVO;
import com.dbs.tpc_benchmark.typings.vo.ShipPriorVO;

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
    
    public String exportClientInfo(ClientInfoVO clientInfoVO, String exportPath) throws Exception {
        if (!verifyExportPath(exportPath)) {
            throw new IllegalArgumentException("Invalid export path: " + exportPath);
        }

        String fileName = "client_info_" + System.currentTimeMillis() + ".csv";
        String filePath = new File(exportPath, fileName).getAbsolutePath();

        ClientInfoDTO clientInfoDTO = new ClientInfoDTO();
        clientInfoDTO.setCurrentPage(1);
        clientInfoDTO.setPageSize(clientInfoVO.getTotal());

        ClientInfoVO newClientInfoVO = queryService.getClientInfo(clientInfoDTO);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            // 表头
            writer.write("Name,Address,Nation");
            writer.newLine();
            // 数据
            for (ClientInfo clientInfo : newClientInfoVO.getClientInfoList()) {
                StringBuilder sb = new StringBuilder();
                sb.append(escapeField(clientInfo.getName())).append(",");
                sb.append(escapeField(clientInfo.getAddress())).append(",");
                sb.append(escapeField(clientInfo.getNationName()));
                writer.write(sb.toString());
                writer.newLine();
            }
        }
        return filePath;
    }

    public String exportShipPrior(ShipPriorVO shipPriorVO, String exportPath) throws Exception {
        if (!verifyExportPath(exportPath)) {
            throw new IllegalArgumentException("Invalid export path: " + exportPath);
        }

        String fileName = "ship_prior_" + System.currentTimeMillis() + ".csv";
        String filePath = new File(exportPath, fileName).getAbsolutePath();
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            // 表头
            writer.write("OrderKey,Revenue,Order Date,ShipPriority");
            writer.newLine();
            // 数据
            for (OrderRevenue order : shipPriorVO.getOrders()) {
                StringBuilder sb = new StringBuilder();
                sb.append(escapeField(order.getOrderKey())).append(",");
                sb.append(escapeField(order.getRevenue())).append(",");
                sb.append(escapeField(order.getOrderDate())).append(",");
                sb.append(escapeField(order.getShipPriority()));
                writer.write(sb.toString());
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