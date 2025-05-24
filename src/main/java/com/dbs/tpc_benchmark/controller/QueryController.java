package com.dbs.tpc_benchmark.controller;

import com.dbs.tpc_benchmark.config.Result;
import com.dbs.tpc_benchmark.service.QueryService;
import com.dbs.tpc_benchmark.typings.dto.ClientInfoDTO;
import com.dbs.tpc_benchmark.typings.vo.ClientInfoVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    @GetMapping("/table-info")
    public Result<?> getTableInfo(@RequestParam("tableName") String tableName) {
        try {
            List<Map<String, Object>> result = queryService.getData(tableName);
            return Result.success(result, "query success");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
