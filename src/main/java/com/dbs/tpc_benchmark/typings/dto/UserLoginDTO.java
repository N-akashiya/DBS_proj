package com.dbs.tpc_benchmark.typings.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * DTO 数据传输对象
 * 用于接受封装前端传输过来的数据
 * UserLoginDTO 接受前端传过来的用户名和密码
 */
@Data
public class UserLoginDTO implements Serializable {
    private String name;
    private String password;
}
