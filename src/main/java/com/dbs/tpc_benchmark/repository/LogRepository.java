package com.dbs.tpc_benchmark.repository;

import com.dbs.tpc_benchmark.typings.entity.Log;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LogRepository extends JpaRepository<Log, Long> {
    List<Log> findByTableNameOrderByImportTimeDesc(String tableName);
}
