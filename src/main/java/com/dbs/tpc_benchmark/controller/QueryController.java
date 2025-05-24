package com.dbs.tpc_benchmark.controller;

import com.dbs.tpc_benchmark.config.Result;
import com.dbs.tpc_benchmark.service.QueryService;
import com.dbs.tpc_benchmark.typings.dto.ClientInfoDTO;
import com.dbs.tpc_benchmark.typings.vo.ClientInfoVO;
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
        if (clientInfoVO == null) {
            return Result.error("Invalid query");
        } else {
            return Result.success(clientInfoVO, "query success");
        }
    }
}
