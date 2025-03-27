package com.dbs.tpc_benchmark.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Param;
import java.util.*;

@Mapper
public interface TableMapper {

    @Update("${ddlStatement}")
    void executeDDL(@Param("ddlStatement") String ddlStatement);
    
    @Select({
        "SELECT table_name FROM information_schema.tables ",
        "WHERE table_schema = DATABASE() AND table_name = #{tableName}"
    })
    String checkTableExists(@Param("tableName") String tableName);

    @Select("SHOW TABLES")
    List<String> getAllTables();

    @Select({
        "SELECT update_time, data_length + index_length AS data_length ",
        "FROM information_schema.tables ",
        "WHERE table_schema = DATABASE() AND table_name = #{tableName}"
    })
    Map<String, Object> getTableInfo(@Param("tableName") String tableName);
}