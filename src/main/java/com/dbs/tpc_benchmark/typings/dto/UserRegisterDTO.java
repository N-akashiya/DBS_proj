package com.dbs.tpc_benchmark.typings.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserRegisterDTO implements Serializable {
    private String name;
    private String password;
}
