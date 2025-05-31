package com.dbs.tpc_benchmark.typings.vo;
import com.dbs.tpc_benchmark.typings.tableList.OrderRevenue;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ShipPriorVO {
    private List<OrderRevenue> orders;
    private int count;

    private long executionTimeMs;
    private double throughputQPS;
    private double avgLatencyMs;
}
