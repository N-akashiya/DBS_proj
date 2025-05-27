package com.dbs.tpc_benchmark.controller;

import com.dbs.tpc_benchmark.config.Result;
import com.dbs.tpc_benchmark.service.tpcCQueryService;
import com.dbs.tpc_benchmark.typings.dto.NewOrderDTO;
import com.dbs.tpc_benchmark.typings.dto.PaymentDTO;
import com.dbs.tpc_benchmark.typings.vo.NewOrderVO;
import com.dbs.tpc_benchmark.typings.vo.PaymentVO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/tpcc")
public class tpcCQueryController {
    @Autowired
    private tpcCQueryService tpccQueryService;

    @PostMapping("/new-order")
    public Result<NewOrderVO> processNewOrder(@RequestBody NewOrderDTO newOrderDTO) {
        try {
            NewOrderVO result = tpccQueryService.processNewOrder(newOrderDTO);
            return Result.success(result, "新订单事务处理成功，用时 " + result.getExecutionTimeMs() + " ms");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("新订单事务处理失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/payment")
    public Result<PaymentVO> processPayment(@RequestBody PaymentDTO paymentDTO) {
        try {
            PaymentVO result = tpccQueryService.processPayment(paymentDTO);
            return Result.success(result, "支付事务处理成功，用时 " + result.getExecutionTimeMs() + " ms");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("支付事务处理失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/execution-plan")
    public Result<Map<String, Object>> getExecutionPlan(
            @RequestParam String sql,
            @RequestParam(required=false, defaultValue="new-order") String transactionType) {
        try {
            Map<String, Object> plan = tpccQueryService.getExecutionPlan(sql, transactionType);
            return Result.success(plan, "获取执行计划成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取执行计划失败: " + e.getMessage());
        }
    }
}
