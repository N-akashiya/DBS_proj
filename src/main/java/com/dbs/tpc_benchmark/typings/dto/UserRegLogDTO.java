package com.dbs.tpc_benchmark.typings.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * DTO 数据传输对象
 * 用于接收前端请求的数据
 */
@Data
public class UserRegLogDTO implements Serializable {
    private String name;
    private String password;
}
