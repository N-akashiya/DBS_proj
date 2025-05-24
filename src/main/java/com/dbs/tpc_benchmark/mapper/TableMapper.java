package com.dbs.tpc_benchmark.mapper;

import com.dbs.tpc_benchmark.typings.tableList.ClientInfo;
import com.dbs.tpc_benchmark.typings.vo.ClientInfoVO;
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

    @Select({
            "<script>",
            "SELECT ",
            "c.C_CUSTKEY AS cuskey, ",
            "c.C_NAME AS name, ",
            "c.C_ADDRESS AS address, ",
            "c.C_PHONE AS phone, ",
            "c.C_ACCTBAL AS acctbal, ",
            "c.C_MKTSEGMENT AS mktsegment, ",
            "c.C_COMMENT AS comment, ",
            "n.N_NAME AS nationName, ",
            "n.N_NATIONKEY AS nationKey, ",
            "n.N_COMMENT AS nationCom, ",
            "r.R_NAME AS regionName, ",
            "r.R_REGIONKEY AS regionKey, ",
            "r.R_COMMENT AS regionCom ",

            "FROM CUSTOMER c ",
            "JOIN NATION n ON c.C_NATIONKEY = n.N_NATIONKEY ",
            "JOIN REGION r ON n.N_REGIONKEY = r.R_REGIONKEY ",

            "WHERE 1 = 1 ",
            "<if test='nameKeyword != null and nameKeyword != \"\"'>",
            "AND c.C_NAME LIKE CONCAT('%', #{nameKeyword}, '%') ",
            "</if>",
            "<if test='nationKeyword != null and nationKeyword != \"\"'>",
            "AND n.N_NAME LIKE CONCAT('%', #{nationKeyword}, '%') ",
            "</if>",
            "ORDER BY c.C_CUSTKEY ASC",
            "LIMIT #{pageSize} OFFSET #{offset} ",
            "</script>"
    })
    List<ClientInfo> getClientInfoPage(@Param("nameKeyword") String nameKeyword,
                                       @Param("nationKeyword") String nationKeyword,
                                       @Param("pageSize") int pageSize,
                                       @Param("offset") int offset);

    @Select({
            "<script>",
            "SELECT COUNT(*) ",
            "FROM CUSTOMER c ",
            "JOIN NATION n ON c.C_NATIONKEY = n.N_NATIONKEY ",
            "JOIN REGION r ON n.N_REGIONKEY = r.R_REGIONKEY ",
            "WHERE 1=1 ",
            "<if test='nameKeyword != null and nameKeyword != \"\"'>",
            "AND c.C_NAME LIKE CONCAT('%', #{nameKeyword}, '%') ",
            "</if>",
            "<if test='nationKeyword != null and nationKeyword != \"\"'>",
            "AND n.N_NAME LIKE CONCAT('%', #{nationKeyword}, '%') ",
            "</if>",
            "</script>"
    })
    int countClientInfo(@Param("nameKeyword") String nameKeyword,
                        @Param("nationKeyword") String nationKeyword);

    @Select({
            "<script>",
            "SELECT * FROM ${tableName}",
            "</script>"
    })
    List<Map<String, Object>> getAllFromTable(@Param("tableName") String tableName);
}