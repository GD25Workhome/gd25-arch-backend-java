package com.lance.testall.lock.dto;

import lombok.Data;

/**
 * 当前库存查询结果。
 */
@Data
public class StockViewResponse {

    private String skuId;
    private Integer stock;
    private Integer version;
}
