package com.dbs.tpc_benchmark.typings.dto;

import lombok.Data;
import java.util.List;

@Data
public class NewOrderDTO {
    private int warehouseId;
    private int districtId;
    private int customerId;
    private List<OrderItemDTO> items;
    
    @Data
    public static class OrderItemDTO {
        private int itemId;
        private int supplierId;
        private int quantity;
    }
}
