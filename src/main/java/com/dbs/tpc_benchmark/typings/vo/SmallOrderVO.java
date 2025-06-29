package com.dbs.tpc_benchmark.typings.vo;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SmallOrderVO {
    private BigDecimal avgrevenue;
    private long executionTimeMs;
    private double throughputQPS;
    private double avgLatencyMs;
}
