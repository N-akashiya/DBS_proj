package com.dbs.tpc_benchmark.typings.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PaymentVO {
    private int warehouseId;
    private String warehouseName;
    private int districtId;
    private String districtName;
    private int customerId;
    private String customerName;
    private LocalDateTime paymentDate;
    private BigDecimal paymentAmount;
    private BigDecimal customerBalanceBeforePayment;
    private BigDecimal customerBalanceAfterPayment;
    private long executionTimeMs;
    private List<SqlExecutionDetail> sqlDetails;
    
    @Data
    @Builder
    public static class SqlExecutionDetail {
        private String sqlType;
        private String description;
        private long executionTimeMs;
    }
}
