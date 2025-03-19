package com.dbs.tpc_benchmark.typings.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
public class UserApprovalVO implements Serializable {
    private int count;              
    private List<String> usernames;
}