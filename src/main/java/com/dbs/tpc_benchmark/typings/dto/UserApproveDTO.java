package com.dbs.tpc_benchmark.typings.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class UserApproveDTO implements Serializable{
    private List<Long> ids;
}
