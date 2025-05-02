package com.dbs.tpc_benchmark.typings.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "log")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Log {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String tableName;
    
    @Column(nullable = false)
    private LocalDateTime importTime;
    
    @Column(nullable = false)
    private int totalRows;
    
    @Column(nullable = false)
    private int successRows;
    
    @Column(nullable = false)
    private int failedRows;
    
    @Column(columnDefinition = "TEXT")
    private String errorLines;
}
