package com.dbs.tpc_benchmark.typings.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * VO 视图对象
 * 用于返回给前端封装好的数据模型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInfoVO implements Serializable {
    private String name;
    private String role;
    private String authorization;
}
