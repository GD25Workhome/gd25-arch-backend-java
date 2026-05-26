package com.lance.testall.lock.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lance.testall.lock.entity.LockDemoStock;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 表 lock_demo_stock 的持久化接口。
 * <p>
 * 实验 0～2：{@link BaseMapper#selectById} + {@link BaseMapper#updateById}（非原子读-改-写）。<br>
 * 实验 4～5：{@link #atomicDecrementStock}、{@link #optimisticDecrementStock}、{@link #selectForUpdate}。
 */
@Mapper
public interface LockDemoStockMapper extends BaseMapper<LockDemoStock> {

    /**
     * 实验 4b：单条 SQL 原子扣减（WHERE stock &gt;= 1）。
     *
     * @return 影响行数，1 表示成功，0 表示库存不足
     */
    @Update("""
            UPDATE lock_demo_stock
            SET stock = stock - 1, version = version + 1, updated_at = CURRENT_TIMESTAMP
            WHERE sku_id = #{skuId} AND stock >= 1
            """)
    int atomicDecrementStock(@Param("skuId") String skuId);

    /**
     * 实验 4a：乐观锁，仅当 version 匹配时扣减。
     *
     * @return 影响行数，0 表示版本冲突或库存不足
     */
    @Update("""
            UPDATE lock_demo_stock
            SET stock = stock - 1, version = version + 1, updated_at = CURRENT_TIMESTAMP
            WHERE sku_id = #{skuId} AND version = #{version} AND stock >= 1
            """)
    int optimisticDecrementStock(@Param("skuId") String skuId, @Param("version") int version);

    /**
     * 实验 5：悲观锁读行（须在事务内调用）。
     */
    @Select("""
            SELECT sku_id, stock, version, updated_at
            FROM lock_demo_stock
            WHERE sku_id = #{skuId}
            FOR UPDATE
            """)
    LockDemoStock selectForUpdate(@Param("skuId") String skuId);
}
