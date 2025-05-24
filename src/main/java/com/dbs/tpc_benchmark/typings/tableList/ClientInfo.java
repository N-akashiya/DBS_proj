package com.dbs.tpc_benchmark.typings.tableList;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClientInfo {
    private int cuskey;
    private String name;
    private String address;
    private String phone;
    private float acctbal;
    private String mktsegment;
    private String comment;

    private String nationName;
    private int nationKey;
    private String nationCom;

    private String regionName;
    private int regionKey;
    private String regionCom;
}
