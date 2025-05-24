package com.dbs.tpc_benchmark.controller;

import com.dbs.tpc_benchmark.config.Result;
import com.dbs.tpc_benchmark.service.QueryService;
import com.dbs.tpc_benchmark.typings.dto.ClientInfoDTO;
import com.dbs.tpc_benchmark.typings.vo.ClientInfoVO;
import com.dbs.tpc_benchmark.typings.vo.ShipPriorVO;
import com.dbs.tpc_benchmark.typings.dto.ShipPriorDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/query")
public class QueryController {
    @Autowired
    private QueryService queryService;

    @PostMapping("/client-info")
    public Result<ClientInfoVO> getClientInfo(@RequestBody ClientInfoDTO clientInfoDTO) {
        ClientInfoVO clientInfoVO = queryService.getClientInfo(clientInfoDTO);
        if (clientInfoVO == null)
            return Result.error("Invalid query");
        return Result.success(clientInfoVO, "Query success");
    
    }

    @PostMapping("/shipping-priority")
    public Result<ShipPriorVO> getShipPrior(@RequestBody ShipPriorDTO shipPriorDTO) {
        // 参数验证
        if (shipPriorDTO.getMarketSegment() == null) 
            return Result.error("Market segment is required");
        if (shipPriorDTO.getOrderDateBefore() == null)
            return Result.error("Order date before is required");
        if (shipPriorDTO.getShipDateAfter() == null)
            return Result.error("Ship date after is required");
        if (shipPriorDTO.getOrderlimit() == null)
            shipPriorDTO.setOrderlimit(10);
        
        ShipPriorVO shipPriorVO = queryService.getShipPrior(shipPriorDTO);
        if (shipPriorVO == null) 
            return Result.error("Invalid query");
        return Result.success(shipPriorVO, "Query success");
    }
}
