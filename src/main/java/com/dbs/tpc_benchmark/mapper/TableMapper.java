package com.dbs.tpc_benchmark.mapper;

import com.dbs.tpc_benchmark.typings.tableList.ClientInfo;
import com.dbs.tpc_benchmark.typings.tableList.OrderRevenue;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
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

    // ShipPrior
    @Select({
        "SELECT ",
        "l.L_ORDERKEY as orderKey, ",
        "SUM(l.L_EXTENDEDPRICE * (1 - l.L_DISCOUNT)) as revenue, ",
        "o.O_ORDERDATE as orderDate, ",
        "o.O_SHIPPRIORITY as shipPriority ",
        "FROM ",
        "CUSTOMER c ",
        "JOIN ORDERS o ON c.C_CUSTKEY = o.O_CUSTKEY ",
        "JOIN LINEITEM l ON l.L_ORDERKEY = o.O_ORDERKEY ",
        "WHERE ",
        "c.C_MKTSEGMENT = #{marketSegment} ",
        "AND o.O_ORDERDATE < #{orderDateBefore} ",
        "AND l.L_SHIPDATE > #{shipDateAfter} ",
        "GROUP BY ",
        "l.L_ORDERKEY, ",
        "o.O_ORDERDATE, ",
        "o.O_SHIPPRIORITY ",
        "ORDER BY ",
        "revenue DESC, ",
        "o.O_ORDERDATE ",
        "LIMIT #{orderlimit}"
    })
    List<OrderRevenue> getShipPriorQuery(
        @Param("marketSegment") String marketSegment,
        @Param("orderDateBefore") LocalDate orderDateBefore,
        @Param("shipDateAfter") LocalDate shipDateAfter,
        @Param("orderlimit") Integer orderlimit
    );

    // SmallOrder
    @Select({
        "SELECT ",
        "SUM(l.L_EXTENDEDPRICE) / #{years} AS avgrevenue ",
        "FROM ",
        "LINEITEM l ",
        "JOIN PART p ON p.P_PARTKEY = l.L_PARTKEY ",
        "WHERE ",
        "p.P_BRAND = #{brand} ",
        "AND p.P_CONTAINER = #{container} ",
        "AND l.L_QUANTITY < ( ",
        "   SELECT ",
        "   0.2 * AVG(l2.L_QUANTITY) ",
        "   FROM ",
        "   LINEITEM l2 ",
        "   WHERE ",
        "   l2.L_PARTKEY = p.P_PARTKEY ",
        ")"
    })
    BigDecimal getSmallOrderQuery(
        @Param("brand") String brand,
        @Param("container") String container,
        @Param("years") Integer years
    );


    @Select({
            "<script>",
            "SELECT * FROM ${tableName}",
            "</script>"
    })
    List<Map<String, Object>> getAllFromTable(@Param("tableName") String tableName);
}