package com.dbs.tpc_benchmark.typings.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProgressVO {
    private String taskId;
    private String tableName;
    private long totalLines;
    private long processedLines;
    private double percentage;
    private String status;
}
