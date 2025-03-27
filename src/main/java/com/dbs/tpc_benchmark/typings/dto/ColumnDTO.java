package com.dbs.tpc_benchmark.typings.dto;

import com.dbs.tpc_benchmark.typings.constant.ColumnType;
import lombok.Data;

@Data
public class ColumnDTO {
    private String name;
    private ColumnType type;
    private Integer length;
    private boolean primaryKey;
    private boolean notNull;
    private Integer upperLimit;
    private Integer lowerLimit;
}
