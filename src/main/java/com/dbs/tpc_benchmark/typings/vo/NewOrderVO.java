package com.dbs.tpc_benchmark.typings.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class NewOrderVO {
    private int orderId;
    private int warehouseId;
    private int districtId;
    private int customerId;
    private LocalDateTime entryDate;
    private BigDecimal totalAmount;
    private List<OrderItemDetailVO> items;
    private long executionTimeMs;
    private double throughputQPS;
    private double avgLatencyMs;
    private List<SqlExecutionDetail> sqlDetails;
    
    @Data
    @Builder
    public static class OrderItemDetailVO {
        private int itemId;
        private String itemName;
        private int supplierId;
        private int quantity;
        private BigDecimal amount;
        private String stockStatus;
        private String brand;
    }
    
    @Data
    @Builder
    public static class SqlExecutionDetail {
        private String sqlType;
        private String description;
        private long executionTimeMs;
    }
}
