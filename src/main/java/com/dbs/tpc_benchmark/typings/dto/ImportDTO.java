package com.dbs.tpc_benchmark.typings.dto;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class ImportDTO {
    private String tableName;
    private MultipartFile file;
}