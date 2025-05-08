package com.dbs.tpc_benchmark.typings.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ImportResultVO {
    private String tableName;
    private String taskId;
//    private int totalRecords;
//    private int importedRecords;
}
