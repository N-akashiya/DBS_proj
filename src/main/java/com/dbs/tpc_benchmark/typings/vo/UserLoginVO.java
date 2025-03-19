package com.dbs.tpc_benchmark.typings.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * VO 视图模型
 * 用于返回给前端封装好的数据模型
 * UserLoginVO 返回响应的信息，状态码，jwt授权的token
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLoginVO implements Serializable {
    private String role; // 用户类型
    private String name; // 用户名称
    private String authorization; // token
}
