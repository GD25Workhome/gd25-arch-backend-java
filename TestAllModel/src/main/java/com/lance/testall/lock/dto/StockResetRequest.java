package com.lance.testall.lock.dto;

import lombok.Data;

/**
 * 重置库存请求。
 */
@Data
public class StockResetRequest {

    private String skuId;
    private Integer stock = 100;
}
