package com.dbs.tpc_benchmark.service;

import com.dbs.tpc_benchmark.mapper.TableMapper;
import com.dbs.tpc_benchmark.typings.dto.ClientInfoDTO;
import com.dbs.tpc_benchmark.typings.dto.ShipPriorDTO;
import com.dbs.tpc_benchmark.typings.dto.SmallOrderDTO;
import com.dbs.tpc_benchmark.typings.tableList.ClientInfo;
import com.dbs.tpc_benchmark.typings.tableList.OrderRevenue;
import com.dbs.tpc_benchmark.typings.vo.ClientInfoVO;
import com.dbs.tpc_benchmark.typings.vo.ShipPriorVO;
import com.dbs.tpc_benchmark.typings.vo.SmallOrderVO;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class QueryService {
    @Autowired
    private TableMapper tableMapper;

    @Transactional
    public ClientInfoVO getClientInfo(ClientInfoDTO clientInfoDTO) {
        int offset = (clientInfoDTO.getCurrentPage() - 1) * clientInfoDTO.getPageSize();
        List<ClientInfo> clientInfoList = tableMapper.getClientInfoPage(clientInfoDTO.getNameKeyword(), clientInfoDTO.getNationKeyword(), clientInfoDTO.getPageSize(), offset);
        int total = tableMapper.countClientInfo(clientInfoDTO.getNameKeyword(), clientInfoDTO.getNationKeyword());
        return ClientInfoVO.builder()
                .clientInfoList(clientInfoList)
                .total(total)
                .currentPage(clientInfoDTO.getCurrentPage())
                .pageSize(clientInfoDTO.getPageSize())
                .build();
    }

    @Transactional
    public ShipPriorVO getShipPrior(ShipPriorDTO dto) {
        if (dto.getMarketSegment() == null || dto.getOrderDateBefore() == null || dto.getShipDateAfter() == null)
            return null;
        Integer limit = dto.getOrderlimit() != null ? dto.getOrderlimit() : 10;
        List<OrderRevenue> orders = tableMapper.getShipPriorQuery(
            dto.getMarketSegment(),
            dto.getOrderDateBefore(),
            dto.getShipDateAfter(),
            limit
        );
        return ShipPriorVO.builder()
            .orders(orders)
            .count(orders != null ? orders.size() : 0)
            .build();
    }

    @Transactional
    public SmallOrderVO getSmallOrder(SmallOrderDTO dto) {
        if (dto.getBrand() == null || dto.getContainer() == null)
            return null;
        Integer years = dto.getYears() != null ? dto.getYears() : 7;
        BigDecimal avgrevenue = tableMapper.getSmallOrderQuery(
            dto.getBrand(),
            dto.getContainer(),
            years
        );
        if (avgrevenue == null)
            avgrevenue = BigDecimal.ZERO;
        return SmallOrderVO.builder()
            .avgrevenue(avgrevenue)
            .build();
    }

    @Transactional
    public List<Map<String, Object>> getData(String tableName) {
        List<String> allowedTables = List.of("ORDERS", "REGION", "NATION", "SUPPLIER", "PART", "PARTSUPP", "CUSTOMER", "LINEITEM");
        if (!allowedTables.contains(tableName.toUpperCase())) {
            throw new IllegalArgumentException("invalid table name: " + tableName);
        }
        return tableMapper.getAllFromTable(tableName);
    }

}
