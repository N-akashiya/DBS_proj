package com.dbs.tpc_benchmark.typings.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PaymentDTO {
    private int warehouseId;
    private int districtId;
    private int customerId;
    private BigDecimal paymentAmount;
}
