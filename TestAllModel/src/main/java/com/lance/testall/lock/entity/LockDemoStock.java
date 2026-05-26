package com.lance.testall.lock.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 锁实验商品库存，对应表 lock_demo_stock。
 */
@Data
@TableName("lock_demo_stock")
public class LockDemoStock {

    @TableId
    private String skuId;

    private Integer stock;

    private Integer version;

    private LocalDateTime updatedAt;
}
