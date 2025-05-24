package com.dbs.tpc_benchmark.typings.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ShipPriorDTO {
    private String marketSegment;
    private LocalDate orderDateBefore;
    private LocalDate shipDateAfter;
    private Integer orderlimit;
}
