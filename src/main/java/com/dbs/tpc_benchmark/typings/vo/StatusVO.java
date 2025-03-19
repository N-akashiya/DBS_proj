package com.dbs.tpc_benchmark.typings.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class StatusVO implements Serializable {
    private String message;
    private Object data;
}
