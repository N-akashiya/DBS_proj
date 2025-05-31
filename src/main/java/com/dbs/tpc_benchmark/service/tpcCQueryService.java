package com.dbs.tpc_benchmark.service;

import com.dbs.tpc_benchmark.typings.dto.NewOrderDTO;
import com.dbs.tpc_benchmark.typings.dto.PaymentDTO;
import com.dbs.tpc_benchmark.typings.vo.NewOrderVO;
import com.dbs.tpc_benchmark.typings.vo.PaymentVO;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class tpcCQueryService {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final long serviceStartTime = System.currentTimeMillis();
    private int tpccQueryCount = 0;
    private long totalExecutionTimeMs = 0;

    // TPC-C新订单事务
    @Transactional
    public NewOrderVO processNewOrder(NewOrderDTO newOrderDTO) {
        long startTime = System.currentTimeMillis();
        List<NewOrderVO.SqlExecutionDetail> sqlDetails = new ArrayList<>();
        long sqlStartTime;
        
        int warehouseId = newOrderDTO.getWarehouseId();
        int districtId = newOrderDTO.getDistrictId();
        int customerId = newOrderDTO.getCustomerId();
        
        boolean allLocal = true;
        for (NewOrderDTO.OrderItemDTO item : newOrderDTO.getItems()) {
            if (item.getSupplierId() != warehouseId) {
                allLocal = false;
                break;
            }
        }

        try {
            // 1. 获取客户信息及仓库税率
            sqlStartTime = System.currentTimeMillis();
            Map<String, Object> customerAndWarehouseData = jdbcTemplate.queryForMap(
                "SELECT c.C_DISCOUNT, c.C_LAST, c.C_CREDIT, w.W_TAX " +
                "FROM c_customer c, c_warehouse w " +
                "WHERE w.W_ID = ? AND c.C_W_ID = w.W_ID AND " +
                "c.C_D_ID = ? AND c.C_ID = ?", 
                warehouseId, districtId, customerId);
                
            BigDecimal customerDiscount = (BigDecimal) customerAndWarehouseData.get("C_DISCOUNT");
            BigDecimal warehouseTax = (BigDecimal) customerAndWarehouseData.get("W_TAX");
                
            sqlDetails.add(NewOrderVO.SqlExecutionDetail.builder()
                .sqlType("SELECT")
                .description("查询客户和仓库信息")
                .executionTimeMs(System.currentTimeMillis() - sqlStartTime)
                .build());
                
            // 2. 获取并更新区域下一个订单ID
            sqlStartTime = System.currentTimeMillis();
            Map<String, Object> districtData = jdbcTemplate.queryForMap(
                "SELECT D_NEXT_O_ID, D_TAX FROM c_district " +
                "WHERE D_ID = ? AND D_W_ID = ?", 
                districtId, warehouseId);
            int nextOrderId = ((Number) districtData.get("D_NEXT_O_ID")).intValue();
            BigDecimal districtTax = (BigDecimal) districtData.get("D_TAX");

            sqlDetails.add(NewOrderVO.SqlExecutionDetail.builder()
                .sqlType("SELECT")
                .description("查询区域信息和下一个订单ID")
                .executionTimeMs(System.currentTimeMillis() - sqlStartTime)
                .build());
                
            // 更新区域下一个订单ID
            sqlStartTime = System.currentTimeMillis();
            jdbcTemplate.update(
                "UPDATE c_district SET D_NEXT_O_ID = D_NEXT_O_ID + 1 " +
                "WHERE D_ID = ? AND D_W_ID = ?",
                districtId, warehouseId);
                
            sqlDetails.add(NewOrderVO.SqlExecutionDetail.builder()
                .sqlType("UPDATE")
                .description("更新区域下一个订单ID")
                .executionTimeMs(System.currentTimeMillis() - sqlStartTime)
                .build());
            
            // 3. 创建新订单
            LocalDateTime now = LocalDateTime.now();
            sqlStartTime = System.currentTimeMillis();
            jdbcTemplate.update(
                "INSERT INTO c_orders (O_ID, O_D_ID, O_W_ID, O_C_ID, " +
                "O_ENTRY_D, O_OL_CNT, O_ALL_LOCAL) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)",
                nextOrderId, districtId, warehouseId, customerId, 
                Timestamp.valueOf(now), newOrderDTO.getItems().size(), 
                allLocal ? 1 : 0);
                
            sqlDetails.add(NewOrderVO.SqlExecutionDetail.builder()
                .sqlType("INSERT")
                .description("插入订单主表记录")
                .executionTimeMs(System.currentTimeMillis() - sqlStartTime)
                .build());
                
            // 4. 插入NEW_ORDER表
            sqlStartTime = System.currentTimeMillis();
            jdbcTemplate.update(
                "INSERT INTO c_new_order (NO_O_ID, NO_D_ID, NO_W_ID) " +
                "VALUES (?, ?, ?)",
                nextOrderId, districtId, warehouseId);
                
            sqlDetails.add(NewOrderVO.SqlExecutionDetail.builder()
                .sqlType("INSERT")
                .description("插入新订单表记录")
                .executionTimeMs(System.currentTimeMillis() - sqlStartTime)
                .build());
                
            // 5. 处理订单行项目
            BigDecimal totalAmount = BigDecimal.ZERO;
            List<NewOrderVO.OrderItemDetailVO> orderItemDetails = new ArrayList<>();
            
            for (int i = 0; i < newOrderDTO.getItems().size(); i++) {
                NewOrderDTO.OrderItemDTO item = newOrderDTO.getItems().get(i);
                int itemId = item.getItemId();
                int supplierId = item.getSupplierId();
                int quantity = item.getQuantity();
                
                try {
                    // 5.1 获取商品信息
                    sqlStartTime = System.currentTimeMillis();
                    Map<String, Object> itemData;
                    try {
                        itemData = jdbcTemplate.queryForMap(
                            "SELECT I_PRICE, I_NAME, I_DATA FROM c_item WHERE I_ID = ?", 
                            itemId);
                    } catch (Exception e) {
                        // 无效商品
                        sqlDetails.add(NewOrderVO.SqlExecutionDetail.builder()
                            .sqlType("ERROR")
                            .description("商品ID无效: " + itemId)
                            .executionTimeMs(System.currentTimeMillis() - sqlStartTime)
                            .build());
                        throw new RuntimeException("无效的商品ID: " + itemId);
                    }
                    
                    sqlDetails.add(NewOrderVO.SqlExecutionDetail.builder()
                        .sqlType("SELECT")
                        .description("查询商品信息 #" + itemId)
                        .executionTimeMs(System.currentTimeMillis() - sqlStartTime)
                        .build());
                        
                    BigDecimal itemPrice = (BigDecimal) itemData.get("I_PRICE");
                    String itemName = (String) itemData.get("I_NAME");
                    String itemData_str = (String) itemData.get("I_DATA");
                    
                    // 5.2 查询库存信息
                    sqlStartTime = System.currentTimeMillis();
                    Map<String, Object> stockData = jdbcTemplate.queryForMap(
                        "SELECT S_QUANTITY, S_DATA, " +
                        "S_DIST_01, S_DIST_02, S_DIST_03, S_DIST_04, S_DIST_05, " +
                        "S_DIST_06, S_DIST_07, S_DIST_08, S_DIST_09, S_DIST_10 " +
                        "FROM c_stock WHERE S_I_ID = ? AND S_W_ID = ?",
                        itemId, supplierId);
                        
                    sqlDetails.add(NewOrderVO.SqlExecutionDetail.builder()
                        .sqlType("SELECT")
                        .description("查询库存信息 #" + itemId)
                        .executionTimeMs(System.currentTimeMillis() - sqlStartTime)
                        .build());
                        
                    // 获取库存数量和配送信息
                    int stockQuantity = ((Number) stockData.get("S_QUANTITY")).intValue();
                    String distInfo = (String) stockData.get("S_DIST_" + String.format("%02d", districtId));
                    String stockData_str = (String) stockData.get("S_DATA");
                    
                    // 5.3 判断brand（B表示original品牌，G表示普通品牌）
                    String brand;
                    if (itemData_str != null && stockData_str != null && 
                        itemData_str.contains("original") && stockData_str.contains("original")) {
                        brand = "B";
                    } else {
                        brand = "G";
                    }

                    // 5.4 更新库存数量
                    int newQuantity;
                    String stockStatus;
                    if (stockQuantity > quantity) {
                        newQuantity = stockQuantity - quantity;
                        stockStatus = "SUFFICIENT";
                    } else {
                        newQuantity = stockQuantity - quantity + 91;
                        stockStatus = "LOW";
                    }
                    
                    sqlStartTime = System.currentTimeMillis();
                    jdbcTemplate.update(
                        "UPDATE c_stock SET S_QUANTITY = ? " +
                        "WHERE S_I_ID = ? AND S_W_ID = ?",
                        newQuantity, itemId, supplierId);
                        
                    sqlDetails.add(NewOrderVO.SqlExecutionDetail.builder()
                        .sqlType("UPDATE")
                        .description("更新库存数量 #" + itemId)
                        .executionTimeMs(System.currentTimeMillis() - sqlStartTime)
                        .build());
                        
                    // 5.5 计算订单项金额
                    BigDecimal amount = itemPrice
                        .multiply(BigDecimal.valueOf(quantity))
                        .multiply(BigDecimal.ONE.add(warehouseTax).add(districtTax))
                        .multiply(BigDecimal.ONE.subtract(customerDiscount))
                        .setScale(2, java.math.RoundingMode.HALF_UP);
                        
                    totalAmount = totalAmount.add(amount);
                    
                    // 5.6 插入订单明细
                    sqlStartTime = System.currentTimeMillis();
                    jdbcTemplate.update(
                        "INSERT INTO c_order_line (OL_O_ID, OL_D_ID, OL_W_ID, OL_NUMBER, " +
                        "OL_I_ID, OL_SUPPLY_W_ID, OL_QUANTITY, OL_AMOUNT, OL_DIST_INFO) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        nextOrderId, districtId, warehouseId, i+1, 
                        itemId, supplierId, quantity, amount, distInfo);
                        
                    sqlDetails.add(NewOrderVO.SqlExecutionDetail.builder()
                        .sqlType("INSERT")
                        .description("插入订单明细 #" + (i+1))
                        .executionTimeMs(System.currentTimeMillis() - sqlStartTime)
                        .build());
                        
                    orderItemDetails.add(NewOrderVO.OrderItemDetailVO.builder()
                        .itemId(itemId)
                        .itemName(itemName)
                        .supplierId(supplierId)
                        .quantity(quantity)
                        .amount(amount)
                        .stockStatus(stockStatus)
                        .brand(brand)
                        .build());
                } catch (Exception e) {
                    // 记录错误并继续处理下一个订单项
                    sqlDetails.add(NewOrderVO.SqlExecutionDetail.builder()
                        .sqlType("ERROR")
                        .description("处理订单项失败: " + e.getMessage())
                        .executionTimeMs(0)
                        .build());
                }
            }
            
            long executionTimeMs = System.currentTimeMillis() - startTime;
            synchronized (this) {
                tpccQueryCount++;
                totalExecutionTimeMs += executionTimeMs;
            }
            long uptimeSeconds = (System.currentTimeMillis() - serviceStartTime) / 1000;
            double throughputQPS = uptimeSeconds > 0 ? (double) tpccQueryCount / uptimeSeconds : 0;
            double avgLatencyMs = tpccQueryCount > 0 ? (double) totalExecutionTimeMs / tpccQueryCount : 0;
            
            // 返回结果
            return NewOrderVO.builder()
                .orderId(nextOrderId)
                .warehouseId(warehouseId)
                .districtId(districtId)
                .customerId(customerId)
                .entryDate(now)
                .totalAmount(totalAmount)
                .items(orderItemDetails)
                .executionTimeMs(executionTimeMs)
                .throughputQPS(throughputQPS)
                .avgLatencyMs(avgLatencyMs)
                .sqlDetails(sqlDetails)
                .build();
            
        } catch (Exception e) {
            // 回滚事务并抛出异常
            throw new RuntimeException("新订单事务处理失败: " + e.getMessage(), e);
        }
    }
    
    // TPC-C支付事务
    @Transactional
    public PaymentVO processPayment(PaymentDTO paymentDTO) {
        long startTime = System.currentTimeMillis();
        List<PaymentVO.SqlExecutionDetail> sqlDetails = new ArrayList<>();
        long sqlStartTime;
        
        int warehouseId = paymentDTO.getWarehouseId();
        int districtId = paymentDTO.getDistrictId();
        int customerId = paymentDTO.getCustomerId();
        BigDecimal paymentAmount = paymentDTO.getPaymentAmount();
        LocalDateTime now = LocalDateTime.now();
        
        String warehouseName = "";
        String districtName = "";
        
        try {
            // 1. 更新仓库信息
            sqlStartTime = System.currentTimeMillis();
            jdbcTemplate.update(
                "UPDATE c_warehouse SET W_YTD = W_YTD + ? WHERE W_ID = ?",
                paymentAmount, warehouseId);
                
            sqlDetails.add(PaymentVO.SqlExecutionDetail.builder()
                .sqlType("UPDATE")
                .description("更新仓库年度销售额")
                .executionTimeMs(System.currentTimeMillis() - sqlStartTime)
                .build());
            
            // 查询仓库信息
            sqlStartTime = System.currentTimeMillis();
            Map<String, Object> warehouseData = jdbcTemplate.queryForMap(
                "SELECT W_STREET_1, W_STREET_2, W_CITY, W_STATE, W_ZIP, W_NAME " +
                "FROM c_warehouse WHERE W_ID = ?", 
                warehouseId);
                
            warehouseName = (String) warehouseData.get("W_NAME");
                
            sqlDetails.add(PaymentVO.SqlExecutionDetail.builder()
                .sqlType("SELECT")
                .description("查询仓库信息")
                .executionTimeMs(System.currentTimeMillis() - sqlStartTime)
                .build());
            
            // 2. 更新区域信息
            sqlStartTime = System.currentTimeMillis();
            jdbcTemplate.update(
                "UPDATE c_district SET D_YTD = D_YTD + ? WHERE D_W_ID = ? AND D_ID = ?",
                paymentAmount, warehouseId, districtId);
                
            sqlDetails.add(PaymentVO.SqlExecutionDetail.builder()
                .sqlType("UPDATE")
                .description("更新区域年度销售额")
                .executionTimeMs(System.currentTimeMillis() - sqlStartTime)
                .build());
                
            // 查询区域信息
            sqlStartTime = System.currentTimeMillis();
            Map<String, Object> districtData = jdbcTemplate.queryForMap(
                "SELECT D_STREET_1, D_STREET_2, D_CITY, D_STATE, D_ZIP, D_NAME " +
                "FROM c_district WHERE D_W_ID = ? AND D_ID = ?", 
                warehouseId, districtId);
                
            districtName = (String) districtData.get("D_NAME");
                
            sqlDetails.add(PaymentVO.SqlExecutionDetail.builder()
                .sqlType("SELECT")
                .description("查询区域信息")
                .executionTimeMs(System.currentTimeMillis() - sqlStartTime)
                .build());
                
            // 3. 查询客户信息
            sqlStartTime = System.currentTimeMillis();
            Map<String, Object> customerData = jdbcTemplate.queryForMap(
                "SELECT C_FIRST, C_MIDDLE, C_LAST, " +
                "C_STREET_1, C_STREET_2, C_CITY, C_STATE, C_ZIP, " +
                "C_PHONE, C_CREDIT, C_CREDIT_LIM, " +
                "C_DISCOUNT, C_BALANCE, C_SINCE " +
                "FROM c_customer WHERE C_W_ID = ? AND C_D_ID = ? AND C_ID = ?",
                warehouseId, districtId, customerId);
                
            sqlDetails.add(PaymentVO.SqlExecutionDetail.builder()
                .sqlType("SELECT")
                .description("查询客户信息")
                .executionTimeMs(System.currentTimeMillis() - sqlStartTime)
                .build());
                
            String customerFirstName = (String) customerData.get("C_FIRST");
            String customerMiddleName = (String) customerData.get("C_MIDDLE");
            String customerLastName = (String) customerData.get("C_LAST");
            String customerName = customerFirstName + " " + customerMiddleName + " " + customerLastName;
            
            BigDecimal customerBalance = (BigDecimal) customerData.get("C_BALANCE");
            String customerCredit = (String) customerData.get("C_CREDIT");
            
            // 计算新客户余额
            BigDecimal newBalance = customerBalance.add(paymentAmount);  // 注意：支付事务增加余额
            
            // 4. 特殊处理不良信用客户
            if ("BC".equals(customerCredit)) {
                // 查询客户数据
                sqlStartTime = System.currentTimeMillis();
                Map<String, Object> customerDataInfo = jdbcTemplate.queryForMap(
                    "SELECT C_DATA FROM c_customer " +
                    "WHERE C_W_ID = ? AND C_D_ID = ? AND C_ID = ?",
                    warehouseId, districtId, customerId);
                    
                String currentData = (String) customerDataInfo.get("C_DATA");
                
                // 格式化新的客户数据
                String customerInfo = String.format("| %4d %2d %4d %2d %4d $%7.2f",
                                                 customerId, districtId, warehouseId,
                                                 districtId, warehouseId, paymentAmount.doubleValue());
                
                // 合并数据，保持在500字符以内
                String customerData_new;
                if (currentData != null && currentData.length() > 0) {
                    customerData_new = customerInfo + currentData;
                    if (customerData_new.length() > 500) {
                        customerData_new = customerData_new.substring(0, 500);
                    }
                } else {
                    customerData_new = customerInfo;
                }
                
                // 更新不良信用客户数据
                sqlStartTime = System.currentTimeMillis();
                jdbcTemplate.update(
                    "UPDATE c_customer SET C_BALANCE = ?, C_DATA = ? " +
                    "WHERE C_W_ID = ? AND C_D_ID = ? AND C_ID = ?",
                    newBalance, customerData_new, warehouseId, districtId, customerId);
                    
                sqlDetails.add(PaymentVO.SqlExecutionDetail.builder()
                    .sqlType("UPDATE")
                    .description("更新不良信用客户信息")
                    .executionTimeMs(System.currentTimeMillis() - sqlStartTime)
                    .build());
            } else {
                // 5. 更新良好信用客户
                sqlStartTime = System.currentTimeMillis();
                jdbcTemplate.update(
                    "UPDATE c_customer SET C_BALANCE = ? " +
                    "WHERE C_W_ID = ? AND C_D_ID = ? AND C_ID = ?",
                    newBalance, warehouseId, districtId, customerId);
                    
                sqlDetails.add(PaymentVO.SqlExecutionDetail.builder()
                    .sqlType("UPDATE")
                    .description("更新良好信用客户信息")
                    .executionTimeMs(System.currentTimeMillis() - sqlStartTime)
                    .build());
            }
            
            // 6. 插入支付历史记录
            // 准备h_data
            String h_data = warehouseName;
            if (h_data.length() > 10) h_data = h_data.substring(0, 10);
            while (h_data.length() < 10) h_data += " ";
            
            String d_name = districtName;
            if (d_name.length() > 10) d_name = d_name.substring(0, 10);
            h_data += d_name;
            while (h_data.length() < 24) h_data += " ";
            
            sqlStartTime = System.currentTimeMillis();
            jdbcTemplate.update(
                "INSERT INTO c_history (H_C_D_ID, H_C_W_ID, H_C_ID, H_D_ID, " +
                "H_W_ID, H_DATE, H_AMOUNT, H_DATA) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                districtId, warehouseId, customerId, districtId,
                warehouseId, Timestamp.valueOf(now), paymentAmount, h_data);
                
            sqlDetails.add(PaymentVO.SqlExecutionDetail.builder()
                .sqlType("INSERT")
                .description("插入支付历史记录")
                .executionTimeMs(System.currentTimeMillis() - sqlStartTime)
                .build());
                
            long executionTimeMs = System.currentTimeMillis() - startTime;
            synchronized (this) {
                tpccQueryCount++;
                totalExecutionTimeMs += executionTimeMs;
            }
            long uptimeSeconds = (System.currentTimeMillis() - serviceStartTime) / 1000;
            double throughputQPS = uptimeSeconds > 0 ? (double) tpccQueryCount / uptimeSeconds : 0;
            double avgLatencyMs = tpccQueryCount > 0 ? (double) totalExecutionTimeMs / tpccQueryCount : 0;
            
            // 返回结果
            return PaymentVO.builder()
                .warehouseId(warehouseId)
                .warehouseName(warehouseName)
                .districtId(districtId)
                .districtName(districtName)
                .customerId(customerId)
                .customerName(customerName)
                .paymentDate(now)
                .paymentAmount(paymentAmount)
                .customerBalanceBeforePayment(customerBalance)
                .customerBalanceAfterPayment(newBalance)
                .executionTimeMs(executionTimeMs)
                .throughputQPS(throughputQPS)
                .avgLatencyMs(avgLatencyMs)
                .sqlDetails(sqlDetails)
                .build();
                
        } catch (Exception e) {
            // 回滚事务并抛出异常
            throw new RuntimeException("支付事务处理失败: " + e.getMessage(), e);
        }
    }
}