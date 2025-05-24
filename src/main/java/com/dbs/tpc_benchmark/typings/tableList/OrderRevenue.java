package com.dbs.tpc_benchmark.typings.tableList;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRevenue {
    private int orderKey;
    private BigDecimal revenue;
    private LocalDate orderDate;
    private int shipPriority; 
}
