package com.dbs.tpc_benchmark.typings.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TableVO {
    private String tablename;
    private String lastupdate;
    @Builder.Default
    private ProgressVO progressVO = null;
}
