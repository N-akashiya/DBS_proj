package com.dbs.tpc_benchmark.typings.dto;

import lombok.Data;
import java.util.List;

@Data
public class TableCreateDTO {
    private String tableName;
    private List<ColumnDTO> columns;
}
