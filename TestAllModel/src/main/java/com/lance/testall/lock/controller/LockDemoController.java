package com.lance.testall.lock.controller;

import com.lance.common.model.ApiResult;
import com.lance.testall.lock.dto.LockRunRequest;
import com.lance.testall.lock.dto.LockRunResponse;
import com.lance.testall.lock.dto.StockResetRequest;
import com.lance.testall.lock.dto.StockViewResponse;
import com.lance.testall.lock.service.LockDemoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 并发锁实验 HTTP 入口（实验 0～5）。
 * <p>
 * 调用链（主路径）：
 * <pre>
 *   LockDemoController
 *     → LockDemoService.run()          // 编排：重置库存、线程池并发、汇总
 *       → StockDeductService.deductOnce()  // 按 lockStrategy 进入不同锁
 *         → deductUnsafe / deductSync* / deductReentrant
 *           → LockDemoStockMapper      // 读-改-写 lock_demo_stock
 *     → lock_demo_run_log 落库
 * </pre>
 * 设计说明见：ai_docs/lock/26052603-TestAllModel-锁实验实现说明.md
 */
@RestController
@RequestMapping("/api/lock-demo")
public class LockDemoController {

    /** 实验编排与库存读写均委托给 Service，Controller 不写业务与锁逻辑 */
    private final LockDemoService lockDemoService;

    public LockDemoController(LockDemoService lockDemoService) {
        this.lockDemoService = lockDemoService;
    }

    /**
     * 运行一批并发扣减实验（核心入口）。
     * <p>
     * 请求体 {@link LockRunRequest#lockStrategy} 决定走无锁 / synchronized / ReentrantLock。
     * 典型参数：initialStock=100，threadCount=200，requestsPerThread=1。
     *
     * @param request 可为 null，将使用 DTO 内默认值（lockStrategy 默认为 NONE）
     * @return 含 batchId、successCount、finalStock、anomaly 等，供对照实验 0～2
     */
    @PostMapping("/run")
    public ApiResult<LockRunResponse> run(@RequestBody(required = false) LockRunRequest request) {
        if (request == null) {
            request = new LockRunRequest();
        }
        try {
            return ApiResult.success(lockDemoService.run(request));
        } catch (IllegalArgumentException ex) {
            // 参数校验失败（threadCount、总任务数上限等），不抛 500
            return ApiResult.fail(ex.getMessage());
        }
    }

    /**
     * 按批次 ID 查询历史实验结果（数据来自 lock_demo_run_log）。
     */
    @GetMapping("/run/{batchId}")
    public ApiResult<LockRunResponse> getRun(@PathVariable String batchId) {
        LockRunResponse response = lockDemoService.getRun(batchId);
        if (response == null) {
            return ApiResult.fail("批次不存在: " + batchId);
        }
        return ApiResult.success(response);
    }

    /**
     * 重置商品库存（实验前准备或手动恢复环境）。
     * 写入表 lock_demo_stock，version 置 0。
     */
    @PostMapping("/stock/reset")
    public ApiResult<StockViewResponse> resetStock(@RequestBody(required = false) StockResetRequest request) {
        if (request == null) {
            request = new StockResetRequest();
        }
        return ApiResult.success(lockDemoService.resetStock(request));
    }

    /**
     * 查询当前库存快照（实验 3b 下走 {@link com.lance.testall.lock.service.StockDeductService#loadStockUnderReadLock} 读锁）。
     */
    @GetMapping("/stock/{skuId}")
    public ApiResult<StockViewResponse> getStock(@PathVariable String skuId) {
        StockViewResponse view = lockDemoService.getStock(skuId);
        if (view == null) {
            return ApiResult.fail("商品不存在: " + skuId);
        }
        return ApiResult.success(view);
    }
}
