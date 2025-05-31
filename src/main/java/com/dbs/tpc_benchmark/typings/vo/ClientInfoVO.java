package com.dbs.tpc_benchmark.typings.vo;

import com.dbs.tpc_benchmark.typings.tableList.ClientInfo;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ClientInfoVO {
    private List<ClientInfo> clientInfoList;
    private int total;
    private int currentPage;
    private int pageSize;

    private long executionTimeMs;   // 执行时间
    private double throughputQPS;   // 吞吐量
    private double avgLatencyMs;    // 平均延迟
}