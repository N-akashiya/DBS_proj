package com.dbs.tpc_benchmark.typings.dto;

import lombok.Data;

@Data
public class ClientInfoDTO {
    private int currentPage;
    private int pageSize;
    private String nationKeyword;
    private String nameKeyword;
}
